package connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlet.classes.FilteredRequest;
import servlet.classes.ResponseData;
import servlet.classes.TableFields;
import utils.Props;

import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.sql.*;
import java.util.*;

public class Connect {

    private static final String SELECT_LAST_OP = "SELECT CASE WHEN count(*) > 1 AND bool_or(h.type_operation = 4) THEN 4 ELSE max(h.type_operation) END as type_operation " +
            "FROM history h WHERE acc_id = %1$s AND timestamp = (SELECT max(timestamp) FROM history WHERE acc_id = %1$s)";
    private static final String UPDATE_IS_ACTIVE = "update public.accounts set is_active = %1$s where accnum = '%2$s' RETURNING id";
    private static final String INSERT_TRANSFER_OPERATIONS = "insert into public.transfer_operations values ('%1$s', '%2$s')";
    private static final String UPDATE_BALANCE = "update public.accounts set balance = '%1$s' where accnum = '%2$s' RETURNING id";
    private static final String VIEW_BY_PAGE = "SELECT * FROM presentation_view %3$s LIMIT '%1$s' OFFSET '%2$s'";
    private static final String SEARCH_ACC_NUM = "select a.accnum from public.accounts a where a.accnum = '%s'";
    private static final String GET_ID_BALANCE = "select id, balance from public.accounts where accnum = '%s'";
    private static final String UPDATE_CLOSE = "update public.accounts set balance = '%1$s', is_active = %2$s where accnum = '%3$s'";
    private static final String INSERT_HISTORY = "insert into public.history values (default, '%1$s', '%2$s', '%3$s', %4$s) RETURNING id";
    private static final String SELECT_ACC_ROW = "select * from public.accounts where accnum = '%s'";
    private static final String SELECT_COUNT_ROWS = "select count(*) from public.%s";
    private static final String CREATE_ACCOUNT = "insert into public.accounts values (default, '%1$s', '%2$s', '%3$s', '%4$s', '%5$s', default) RETURNING id";

    private Connection connection;
    private Statement statement;
    private boolean success = true;
    private static final BigDecimal LIMIT_MONEY = new BigDecimal("10000000");

    Logger LOG = LoggerFactory.getLogger(Connect.class);

    public Connect() {
        openConnection();
    }

    /**
     * установка соединения с бд
     */
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
            connection = DriverManager.getConnection(Props.get("db.url"), Props.get("db.name"), Props.get("db.pass"));
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

    /**
     * создание аккаунта
     * @param s фамилия
     * @param f имя
     * @param p отчество
     * @return true/false
     * @throws SQLException
     */
    public boolean createAcc(String s, String f, String p) throws SQLException {
        connection.setAutoCommit(false);
        try {
            String accId = getColumnList(String.format(CREATE_ACCOUNT, s, f, p, generateAccNum(), 0), "id").get(0);
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

    /**
     * закрытие аккаунта
     * 1. table (accounts) - is_active = false, balance = 0
     * 2. table (history) - Снимаем деньги до 0, Закрываем.
     * @param accNum
     * @return
     * @throws SQLException
     */
    public boolean closeAcc(String accNum) throws SQLException {
        connection.setAutoCommit(false);
        try {
            checkAccNum(accNum);
            String sqlSel = String.format(GET_ID_BALANCE, accNum);
            ResultSet rs = statement.executeQuery(sqlSel);
            Integer accId;
            BigDecimal balance;
            if (rs.next()) {
                accId = rs.getInt("id");
                balance = rs.getBigDecimal("balance");
            } else {
                throw new SQLException("Такого аккаунта нет в Базе!");
            }
            String sqlUpd = String.format(UPDATE_CLOSE, 0, "false", accNum);
            statement.execute(sqlUpd);
            statement.execute(String.format(INSERT_HISTORY, accId, 3, balance.negate(), "CURRENT_TIMESTAMP"));
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

    /**
     * проверка валидности номера аккаунта
     * personName дефолтно - клиент
     * @param accNum номер аккаунта
     * @throws SQLException
     * @throws InvalidParameterException
     */
    private void checkAccNum(String accNum) throws SQLException, InvalidParameterException {
        checkAccNum(accNum, "клиента ");
    }

    /**
     * проверка валидности номера аккаунта
     * 1. проверка длицы строчного номера аккаунта
     * 2. проверка на существование аккаунта в бд
     * 3. если аккаунт неактивный - дополнительные проверки checkNonActiveAcc
     * @param accNum номер аккаунта
     * @param personName "киент "/"получатель "
     * @throws SQLException
     * @throws InvalidParameterException
     */
    private void checkAccNum(String accNum, String personName) throws SQLException, InvalidParameterException{
        if (accNum.length() > 16) {
            throw new InvalidParameterException("Счет ".concat(personName).concat("слишком длинный. Должен быть 16 символов."));
        }
        if (accNum.length() < 16) {
            throw new InvalidParameterException("Счет ".concat(personName).concat("слишком короткий. Должен быть 16 символов."));
        }
        ResultSet rs = statement.executeQuery(String.format(SELECT_ACC_ROW, accNum));
        Integer accId = null;
        boolean isActive = false;
        while (rs.next()) {
            accId = rs.getInt("id");
            isActive = rs.getBoolean("is_active");
        }
        if (accId == null) {
            throw new NullPointerException("Счёта ".concat(personName).concat("с этим номером не существует"));
        } else {
            if (!isActive) {
                checkNonActiveAcc(accId, personName);
            }
        }
    }

    /**
     * ловит исключения работы с неактивными аккаунтами
     * 1. с закрытыми аккаунтами
     * 2. с заблокированными аккаунтами
     * 3. серверные ошибки/сбои
     * @param accId
     * @param personName
     * @throws SQLException
     */
    private void checkNonActiveAcc(int accId, String personName) throws SQLException {
        String lastOpQ = String.format(SELECT_LAST_OP, accId);
        LOG.info(lastOpQ);
        ResultSet rs = statement.executeQuery(lastOpQ);
        if (rs.next()) {
            int lastOp = rs.getInt("type_operation");
            switch (lastOp) {
                case 4:
                    throw new UnsupportedOperationException("Счёт ".concat(personName).concat("закрыт"));
                case 5:
                    throw new UnsupportedOperationException("Счёт ".concat(personName).concat("заблокирован"));
                default:
                    throw new Error("Ошибка в структуре базы. Обратитесь к администратору!");
            }
        } else {
            throw new NullPointerException("В истории нет записи по счёту ".concat(personName).trim());
        }
    }

    /**
     * блокировка аккаунта
     * 1. table (accounts) - флаг is_active = false
     * 2. table (history) - операция "Закрытие".
     * @param accNum номер аккаунта
     * @return true/false
     * @throws SQLException
     */
    public boolean blockAcc(String accNum) throws SQLException {
        connection.setAutoCommit(false);
        checkAccNum(accNum);
        try {
            String sqlUpd = String.format(UPDATE_IS_ACTIVE, "false", accNum);
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

    /**
     * перевод денег другому клиенту банка
     * 1. проверка двух номеров аккаунтов
     * 2. выполнение операций пополнение и снятие внури одной транзацкии (time_stamp один на 2 операции)
     * 3. операции связываются в таблице transfer_operations по id из history
     * @param accNumFrom номер аккаунта отправителя
     * @param accNumTo номер аккаунта получателя
     * @param money сумма денег
     * @return true/false
     * @throws SQLException
     * @throws InvalidAlgorithmParameterException
     */
    public boolean transfer(String accNumFrom, String accNumTo, BigDecimal money) throws SQLException, InvalidAlgorithmParameterException, InvalidParameterException {
        connection.setAutoCommit(false);
        try {
            checkAccNum(accNumFrom, "клиента ");
            checkAccNum(accNumTo, "получателя ");
            if (accNumFrom.equals(accNumTo)) {
                throw new InvalidParameterException("Номер заказчика не может быть равен номеру получателя!");
            }
            int historyIdFrom = transfer(accNumFrom, money.negate(), false);
            int historyIdTo = transfer(accNumTo, money, false);
            String insTransfer = String.format(INSERT_TRANSFER_OPERATIONS, historyIdFrom, historyIdTo);
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

    /**
     * перевод/снятие денег (зависит от знака +-)
     * по дефолту стоит автокоммит транзакции
     * @param accNum номер аккаунта
     * @param money сумма денег
     * @return true/false
     * @throws SQLException
     * @throws InvalidAlgorithmParameterException
     */
    public boolean transfer(String accNum, BigDecimal money) throws SQLException, InvalidAlgorithmParameterException {
        return transfer(accNum, money, true) != null;
    }

    /**
     * перевод/снятие денег (зависит от знака +-)
     * @param accNum номер аккаунта
     * @param money сумма денег
     * @param commitTran AutoCommit true/false
     * @return id из history
     * @throws SQLException
     * @throws InvalidAlgorithmParameterException
     */
    private Integer transfer(String accNum, BigDecimal money, boolean commitTran) throws SQLException, InvalidAlgorithmParameterException {
        checkAccNum(accNum);
        Integer historyId = null;
        if (commitTran) {
            connection.setAutoCommit(false);
        }
        try {
            int accId = updateBalance(accNum, money);
            if (money.compareTo(BigDecimal.ZERO) == 0) {
                throw new InvalidAlgorithmParameterException("Вы не можете выполнить операцию с нулевой суммой!");
            }
            int typeOperation = money.compareTo(BigDecimal.ZERO) < 0 ? 3 : 2;
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

    /**
     * изменение баланса с проверкой исключений
     * @param accNum номер аккаунта
     * @param money сумма денег
     * @return id аккаунта
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    private Integer updateBalance(String accNum, BigDecimal money) throws SQLException, IllegalArgumentException {
        Map<String, String> rowMap = getRowByAccNum(accNum);
        BigDecimal balance = new BigDecimal(rowMap.get("balance"));
        BigDecimal targetBalance = balance.add(money);
        LOG.info("Денег после операции: " + targetBalance.toString());
        LOG.info(String.valueOf(targetBalance.compareTo(LIMIT_MONEY)));
        if (targetBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Баланс не может быть отрицательным!");
        }
        if (targetBalance.compareTo(LIMIT_MONEY) >= 0) {
            throw new IllegalArgumentException("Результатом операции не может быть сумма, превышающая 10 миллионов!");
        }
        String sqlUpd = String.format(UPDATE_BALANCE, targetBalance, accNum);
        ResultSet rs = statement.executeQuery(sqlUpd);
        Integer accId = null;
        while (rs.next()) {
            accId = rs.getInt("id");
        }
        return accId;
    }

    /**
     * динамический филтр (не реализован на фронте)
     * @param fr отфилтрованный запрос
     * @return
     */
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

    /**
     * подготовка ответа на фронт по номеру страницы
     * @param numPage номер страницы из запроса
     * @param lRows лимит строк в странице
     * @param hideClosedAccNums  показывать/нет закрытые/заблокированный аккаунты
     * @return ResponseData
     * @throws SQLException
     * @throws NoSuchFieldException
     */
    public ResponseData getResponseDataByPage(String numPage, String lRows, String hideClosedAccNums) throws SQLException, NoSuchFieldException {
        boolean hideClosed = Boolean.parseBoolean(hideClosedAccNums);
        int count = getCountRows("presentation_view");
        int pageNumber = Integer.parseInt(numPage);
        int limitRows = Integer.parseInt(lRows);
        checkPageNumber(pageNumber, limitRows, count);
        return new ResponseData(getViewByPage(pageNumber, limitRows, hideClosed, ""), String.valueOf(count));
    }

    /**
     * подготовка ответа на фронт (не реализован на фронте)
     * @param filteredRequest отфилтрованный запрос
     * @return ResponseData
     * @throws SQLException
     * @throws NoSuchFieldException
     */
    public ResponseData getResponseDataByPage(FilteredRequest filteredRequest) throws SQLException, NoSuchFieldException {
        int count = getCountRows("presentation_view");
        int pageNumber = Integer.parseInt(filteredRequest.getNumPage());
        int limitRows = Integer.parseInt(filteredRequest.getLimitRows());
        boolean hideClosed = Boolean.parseBoolean(filteredRequest.getHideClosed());
        checkPageNumber(pageNumber, limitRows, count);
        String filter = buildFilter(filteredRequest);
        return new ResponseData(getViewByPage(pageNumber, limitRows, hideClosed, filter), String.valueOf(count));
    }

    /**
     * проверка на существование страницы по запросу
     * @param pageNum номер страницы из запроса
     * @param limitRows лимит строк на странице
     * @param countRows количество строк всего в view
     * @throws NoSuchFieldException
     */
    private void checkPageNumber(int pageNum, int limitRows, int countRows) throws NoSuchFieldException {
        if (pageNum * (limitRows - 1) > countRows) {
            throw new NoSuchFieldException("Такой страницы не существует");
        }
    }

    /**
     * для отправки на фронт данных из view c пагинацией
     * @param numPage номер страницы
     * @param limitRows лимит строк (приходит с фронта)
     * @param hideClosed показывать/нет закрытые/заблокированный аккаунты
     * @param filter фильтр (на фронте не реализован)
     * @return массив TableFields
     * @throws SQLException
     */
    private List<TableFields> getViewByPage(int numPage, int limitRows, boolean hideClosed, String filter) throws SQLException {
        int startRow = numPage * limitRows;
        if (hideClosed) {
            String condition = "is_active = TRUE ";
            filter = "".equals(filter) ? " WHERE ".concat(condition) : filter.concat(" AND ").concat(condition);
        }
        String sqlPresentation = String.format(VIEW_BY_PAGE, limitRows, startRow, filter);
        return getFromView(sqlPresentation);
    }

    /**
     * считает количество строк в заданной таблице
     * @param nameTable имя таблицы
     * @return кол-во строк
     * @throws SQLException
     */
    private Integer getCountRows(String nameTable) throws SQLException {
        Integer count = null;
        ResultSet rs = statement.executeQuery(String.format(SELECT_COUNT_ROWS, nameTable));
        if (rs.next()) {
            count = rs.getInt("count");
        }
        return count;
    }

    /**
     * массив TableFields по запросу
     * @param sql
     * @return массив TableFields
     * @throws SQLException
     */
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
        return ltf;
    }

    /**
     * row с аккаунтом
     * @param accNum номер аккаунта
     * @return row из accounts (все значения Sting)
     * @throws SQLException
     */
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

    /**
     * проверка наличия хотя бы однйо строки в бд по условию из селекта
     * @param select
     * @return
     * @throws SQLException
     */
    private boolean isContainRows(String select) throws SQLException {
        ResultSet rs = statement.executeQuery(select);
        while (rs.next()) {
            return true;
        }
        return false;
    }

    /**
     * массив значений одного столбца по названию и селекту
     * @param select
     * @param column имя столбца
     * @return
     * @throws SQLException
     */
    private List<String> getColumnList(String select, String column) throws SQLException {
        ResultSet rs = statement.executeQuery(select);
        List<String> list = new ArrayList<>();
        while (rs.next()) {
            list.add(rs.getString(column));
        }
        return list;
    }

    /**
     * генетарор случайного номара аккаунта (16 цифр)
     * с проверкой уникальности
     * @return новый уникальный номер аккаунта
     */
    private String generateAccNum() {
        String accNum = generateRndAccNum();
        try {
            while (isContainRows(String.format(SEARCH_ACC_NUM, accNum))) {
                accNum = generateRndAccNum();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accNum;
    }

    /**
     * генератор случайного номера аккаунта
     * может начинаться с [0]+
     * @return квазиуникальный номер аккаунта
     */
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

    /**
     * генератор стучайного числа L
     * @param max максимальный диапазон
     * @return
     */
    private long generateRndLong(long max) {
        return (long) (new Random().nextDouble() * max);
    }
}
