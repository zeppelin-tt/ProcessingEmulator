package connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlet.FilteredRequest;
import servlet.ResponseData;
import servlet.TableFields;

import java.sql.*;
import java.util.*;

public class Connect {

    private Connection connection;
    private Statement statement;
    private static final String INSERT_HISTORY = "insert into public.history values (default, '%1$s', '%2$s', '%3$s', %4$s) RETURNING id";
    private static final String SELECT_ACC_ROW = "select * from public.accounts where accnum = '%s'";
    private static final String SELECT_COUNT_ROWS = "select count(*) from public.%s";
    private boolean success = true;

    Logger LOG = LoggerFactory.getLogger(Connect.class);
    public static final int LIMIT_ROWS = 10;

    public Connect() throws SQLException {
        openConnection();
    }

    private void openConnection() {
        LOG.info("PostgreSQL JDBC Connection Testing");
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOG.error("Include PostgreSQL JDBC Driver in your library path!");
            e.printStackTrace();
            return;
        }
        LOG.info("PostgreSQL JDBC Driver Registered!");
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost/postgres", "postgres", "11121987");
        } catch (SQLException e) {
            LOG.error("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }
        if (connection != null) {
            LOG.info("Control over the base is received.");
        } else {
            LOG.error("Failed to make connection!");
        }
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean createAcc(String s, String f, String p) throws SQLException {
        String sqlAccount = "insert into public.accounts values (default, '%1$s', '%2$s', '%3$s', '%4$s', '%5$s', default) RETURNING id";
        connection.setAutoCommit(false);
        try {
            String accId = getColumnList(String.format(sqlAccount, s, f, p, generateAccNum(), 0), "id").get(0);
            statement.execute(String.format(INSERT_HISTORY, accId, 1, 0, "CURRENT_TIMESTAMP"));
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            success = false;
            e.printStackTrace();
        } finally {
            connection.setAutoCommit(true);
        }
        return success;
    }

    public boolean closeAcc(String accNum) throws SQLException {
        connection.setAutoCommit(false);
        try {
            checkAccNum(accNum);
            String sqlSel = String.format("select id, balance from public.accounts where accnum = '%s'", accNum);
            ResultSet rs = statement.executeQuery(sqlSel);
            Integer accId = null;
            Float balance = null;
            while (rs.next()) {
                accId = rs.getInt("id");
                balance = rs.getFloat("balance");
            }
            String sqlUpd = String.format("update public.accounts set balance = '%1$s', is_active = %2$s where accnum = '%3$s'", 0, "false", accNum);
            statement.execute(sqlUpd);
            statement.execute(String.format(INSERT_HISTORY, accId, 3, -balance, "CURRENT_TIMESTAMP"));
            // TODO: 20.07.2018 может сделать timestamp неуникальным?
//            statement.execute(String.format(INSERT_HISTORY, accId, 4, 0, "CURRENT_TIMESTAMP + INTERVAL '0.000001' SECOND"));
            statement.execute(String.format(INSERT_HISTORY, accId, 4, 0, "CURRENT_TIMESTAMP"));
        } catch (SQLException e) {
            connection.rollback();
            success = false;
            e.printStackTrace();
        } finally {
            connection.setAutoCommit(true);
        }
        return success;
    }

    // TODO: 20.07.2018 или все же сделать булевым метод?
    private void checkAccNum(String accNum) throws SQLException {
        ResultSet rs = statement.executeQuery(String.format(SELECT_ACC_ROW, accNum));
        Integer accId = null;
        boolean isActive = false;
        while (rs.next()) {
            accId = rs.getInt("id");
            isActive = rs.getBoolean("is_active");
        }
        if (accId == null) {
            throw new NullPointerException("Счета с этим номером не существует");
        } else {
            if (!isActive) {
                checkNonActiveAcc(accId);
            }
        }
    }

    private void checkNonActiveAcc(int accId) throws SQLException {
        // TODO: 21.07.2018 не уверен, что это самый оптимальный селект
        String lastOpQ = String.format("SELECT type_operation FROM history where acc_id = %1$s AND timestamp = (SELECT max(timestamp) FROM history WHERE acc_id = %1$s)", accId);
        ResultSet rs = statement.executeQuery(lastOpQ);
        if (rs.next()) {
            int lastOp = rs.getInt("type_operation");
            switch (lastOp) {
                case 4:
                    throw new UnsupportedOperationException("Этот счет закрыт");
                case 5:
                    throw new UnsupportedOperationException("Этот счет заблокирован");
                default:
                    throw new Error("Ошибка в структуре базы. Обратитесь к администратору!");
            }
        } else {
            throw new NullPointerException("В истории нет записи по данному счету");
        }
    }

    public boolean blockAcc(String accNum) throws SQLException {
        connection.setAutoCommit(false);
        checkAccNum(accNum);
        try {
            String sqlUpd = String.format("update public.accounts set is_active = %1$s where accnum = '%2$s' RETURNING id", "false", accNum);
            ResultSet rs = statement.executeQuery(sqlUpd);
            Integer accId = null;
            while (rs.next()) {
                accId = rs.getInt("id");
            }
            statement.execute(String.format(INSERT_HISTORY, accId, 5, 0, "CURRENT_TIMESTAMP"));
        } catch (SQLException e) {
            connection.rollback();
            success = false;
            e.printStackTrace();
        } finally {
            connection.setAutoCommit(true);
        }
        return success;
    }

    // TODO: 02.08.2018 при переводе на заблокированный счет дениги снимаются. Добавить исключение и ролбэк
    public boolean transfer(String accNumFrom, String accNumTo, float money) throws SQLException {
        connection.setAutoCommit(false);
        try {
            int historyIdFrom = transfer(accNumFrom, -money, false);
            int historyIdTo = transfer(accNumTo, money, false);

            String insTransfer = String.format("insert into public.transfer_operations values ('%1$s', '%2$s')", historyIdFrom, historyIdTo);
            statement.execute(insTransfer);
        } catch (SQLException e) {
            connection.rollback();
            success = false;
            e.printStackTrace();
        } finally {
            connection.setAutoCommit(true);
        }
        return success;
    }

    // TODO: 20.07.2018 запрет на работу с неактуальными счетами
    public boolean transfer(String accNum, float money) throws SQLException {
        return transfer(accNum, money, true) != null;
    }

    private Integer transfer(String accNum, float money, boolean commitTran) throws SQLException {
        checkAccNum(accNum);
        Integer historyId = null;
        if (commitTran) {
            connection.setAutoCommit(false);
        }
        try {
            int accId = updateBalance(accNum, money);
            // TODO: 19.07.2018 что с 0 лучше сделать?
            int typeOperation = money > 0 ? 2 : 3;
            ResultSet rs = statement.executeQuery(String.format(INSERT_HISTORY, accId, typeOperation, money, "CURRENT_TIMESTAMP"));
            while (rs.next()) {
                historyId = rs.getInt("id");
            }
        } catch (SQLException e) {
            if (commitTran) {
                connection.rollback();
            }
            success = false;
            e.printStackTrace();
        } finally {
            if (commitTran) {
                connection.setAutoCommit(true);
            }
        }
        return historyId;
    }

    private Integer updateBalance(String accNum, float money) throws SQLException {
        Map<String, String> rowMap = getRowByAccNum(accNum);
        float balance = Float.valueOf(rowMap.get("balance"));
        float targetBalance = balance + money;
        if (targetBalance < 0) {
            // TODO: 19.07.2018 какое здесь лучше выкинуть?
            throw new IllegalArgumentException("Баланс не может быть отрицательным.");
        }
        if (targetBalance > 10000000) {
            throw new IllegalArgumentException("У вас больше 10 миллионов! Поделитесь!");
        }
        String sqlUpd = String.format("update public.accounts set balance = '%1$s' where accnum = '%2$s' RETURNING id", targetBalance, accNum);
        ResultSet rs = statement.executeQuery(sqlUpd);
        Integer accId = null;
        while (rs.next()) {
            accId = rs.getInt("id");
        }
        return accId;
    }


    public String buildFilter(FilteredRequest fr) {
        StringBuilder filter = new StringBuilder();
        filter.append("where");
        String accNum = fr.getAccNum();
        String initials = fr.getInitials();
        String balance = fr.getBalance();
        String action = fr.getAction();
        String lastOpTime = fr.getLastOpTime();
        String createTime = fr.getCreateTime();
        if (accNum != null) {
            filter.append(" accnum ~* '").append(accNum).append("'");
        }
        if (initials != null) {
            filter.append(" initials ~* '").append(initials).append("'");
        }
        if (balance != null) {
            filter.append(" balance ~* '").append(balance).append("'");
        }
        if (action != null) {
            filter.append(" action ~* '").append(action).append("'");
        }
        if (lastOpTime != null) {
            filter.append(" last_operation_time ~* '").append(lastOpTime).append("'");
        }
        if (createTime != null) {
            filter.append(" create_time ~* '").append(createTime).append("'");
        }
        return filter.toString();
    }


    public ResponseData getResponseDataByPage(String numPage, String lRows) throws SQLException, NoSuchFieldException {
        int count = getCountRows("presentation_view");
        int pageNumber = Integer.parseInt(numPage);
        int limitRows = Integer.parseInt(lRows);
        checkPageNumber(pageNumber, limitRows, count);
        return new ResponseData(getViewByPage(pageNumber, limitRows,""), String.valueOf(count));
    }

    public ResponseData getResponseDataByPage(FilteredRequest filteredRequest) throws SQLException, NoSuchFieldException {
        int count = getCountRows("presentation_view");
        int pageNumber = Integer.parseInt(filteredRequest.getNumPage());
        int limitRows = Integer.parseInt(filteredRequest.getLimitRows());
        checkPageNumber(pageNumber, limitRows, count);
        String filter = buildFilter(filteredRequest);
        return new ResponseData(getViewByPage(pageNumber, limitRows, filter), String.valueOf(count));
    }

    private void checkPageNumber(int pageNum, int limitRows, int countRows) throws NoSuchFieldException {
        if (pageNum * (limitRows - 1) > countRows) {
            throw new NoSuchFieldException("Такой страницы не существует");
        }
    }

    private List<TableFields> getViewByPage(int numPage, int limitRows, String filter) throws SQLException {
        int startRow = numPage * limitRows;
        String sqlPresentation = String.format("SELECT * FROM presentation_view %3$s LIMIT '%1$s' OFFSET '%2$s'", limitRows, startRow, filter);
//        LOG.info("GET query: " + sqlPresentation);
        return getFromView(sqlPresentation);
    }

    private Integer getCountRows(String nameTable) throws SQLException {
        Integer count = null;
        ResultSet rs = statement.executeQuery(String.format(SELECT_COUNT_ROWS, nameTable));
        if (rs.next()) {
            count = rs.getInt("count");
        }
        return count;
    }

    private List<TableFields> getFromView(String sql) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        ResultSet rs = preparedStatement.executeQuery();
        List<TableFields> ltf = new ArrayList<>();

        while (rs.next()) {
            ltf.add(new TableFields(
                    rs.getString("id"),
                    rs.getString("accnum"),
                    rs.getString("initials"),
                    rs.getString("balance"),
                    rs.getString("action"),
                    rs.getTimestamp("last_operation_time"),
                    rs.getTimestamp("create_time")
            ));
        }
//        ltf.forEach(e -> LOG.info(e.toString()));
        return ltf;
    }

    private Map<String, String> getRowByAccNum(String accNum) throws SQLException {
        Map<String, String> rowMap = new HashMap<>();
        ResultSet rs = statement.executeQuery(String.format(SELECT_ACC_ROW, accNum));
        while (rs.next()) {
            int countColumns = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= countColumns; i++) {
                rowMap.put(rs.getMetaData().getColumnName(i), rs.getString(i));
            }
        }
        return rowMap;
    }

    private boolean isContainRows(String select) throws SQLException {
        ResultSet rs = statement.executeQuery(select);
        while (rs.next()) {
            return true;
        }
        return false;
    }

    private List<String> getColumnList(String select, String column) throws SQLException {
        ResultSet rs = statement.executeQuery(select);
        List<String> list = new ArrayList<>();
        while (rs.next()) {
            list.add(rs.getString(column));
        }
        return list;
    }

    private String generateAccNum() {
        String accNum = generateRndAccNum();
        try {
            while (isContainRows(String.format("select a.accnum from public.accounts a where a.accnum = '%s'", accNum))) {
                accNum = generateRndAccNum();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accNum;
    }

    private String generateRndAccNum() {
        StringBuilder sb = new StringBuilder();
        String numStr = String.valueOf(generateRndLong((long) Math.pow(10, 16) - 1));
        int needZero = 16 - numStr.length();
        for (int i = 0; i < needZero; i++) {
            sb.append("0");
        }
        sb.append(numStr);
        return sb.toString();
    }

    private long generateRndLong(long max) {
        return (long) (new Random().nextDouble() * max);
    }
}
