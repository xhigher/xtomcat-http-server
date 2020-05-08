package com.cheercent.xtomcat.httpserver.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * @copyright (c) xhigher 2015
 * @author xhigher    2015-3-26
 */
public abstract class XModel {

    protected static Logger logger = LoggerFactory.getLogger(XModel.class);

    private List<String> mFields = new ArrayList<String>();
    private List<ClauseWhere> mWheres = new ArrayList<ClauseWhere>();
    private List<ClauseValue> mValues = new ArrayList<ClauseValue>();
    private List<Object> mUpdateFields = new ArrayList<>();

    private List<List<ClauseValue>> mBatchValues = new ArrayList<List<ClauseValue>>();

    private String mStatementSQL = null;
    private List<Object> mStatementParams = new ArrayList<Object>();
    private List<List<Object>> mBatchStatementParams = new ArrayList<List<Object>>();
    private LinkedHashMap<String, Boolean> mOrderList = new LinkedHashMap<>();
    private String mGroup = null;
    private String mLimit = null;
    private boolean mDistinct = false;
    private SQLException mException = null;

    private XContext mContext = null;

    protected abstract String getDataSourceName();

    protected abstract String tableName();

    private boolean isBadTransaction() {
        return (mContext == null || mContext.getTransaction() == null || mContext.getTransaction().isEnded());
    }

    public boolean setTransaction(XContext context) {
        mContext = context;
        if (mContext != null && mContext.getTransaction() != null) {
            if (mContext.getTransaction().getConnection() == null) {
                return mContext.getTransaction().setConnection(XMySQL.getConnection(getDataSourceName()));
            }
            return true;
        }
        return false;
    }

    public Connection getConnection() {
        if (!isBadTransaction()) {
            return mContext.getTransaction().getConnection();
        }
        return XMySQL.getConnection(getDataSourceName());
    }

    public void closeConnection(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                logger.error("XModel.closeConnection.Exception", e);
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (Exception e) {
                logger.error("XModel.closeConnection.Exception", e);
            }
        }

        if (isBadTransaction()) {
            XMySQL.releaseConnection(conn);
        }
    }

    public JSONArray selectBySQL(String sql, List<Object> data) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    pstmt.setObject(i + 1, data.get(i));
                }
            }
            rs = pstmt.executeQuery();
            logger.info("selectBySQL: " + readOriginalSql(pstmt));
            return this.getJSONArray(rs);
        } catch (SQLException e) {
            logger.error("XModel.selectBySQL.SQLException:" + sql, e);
        } finally {
            this.closeConnection(rs, pstmt, conn);
        }
        return null;
    }

    public JSONArray selectBySQL(String sql, Object... data) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            if (data != null) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
            }
            logger.info("selectBySQL: " + readOriginalSql(pstmt));
            rs = pstmt.executeQuery();
            return this.getJSONArray(rs);
        } catch (SQLException e) {
            logger.error("XModel.selectBySQL.SQLException: " + readOriginalSql(pstmt), e);
        } finally {
            this.closeConnection(rs, pstmt, conn);
        }
        return null;
    }

    public String readOriginalSql(PreparedStatement pstmt) {
        try {
            Field delegate = pstmt.getClass().getSuperclass().getSuperclass().getDeclaredField("delegate");
            if (!delegate.isAccessible()) {
                delegate.setAccessible(true);
            }

            Object stat = delegate.get(pstmt);
            Method method = stat.getClass().getDeclaredMethod("asSql");
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return (String) method.invoke(stat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public JSONArray selectBySQL(String sql) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            logger.info("selectBySQL: " + readOriginalSql(pstmt));
            return this.getJSONArray(rs);
        } catch (SQLException e) {
            logger.error("XModel.selectBySQL.SQLException: " + sql, e);
        } finally {
            this.closeConnection(rs, pstmt, conn);
        }
        return null;
    }

    public JSONObject findBySQL(String sql, List<Object> data) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    pstmt.setObject(i + 1, data.get(i));
                }
            }
            rs = pstmt.executeQuery();
            logger.info("findBySQL: " + readOriginalSql(pstmt));
            return this.getJSONObject(rs);
        } catch (SQLException e) {
            logger.error("XModel.findBySQL.SQLException: " + sql, e);
        } finally {
            this.closeConnection(rs, pstmt, conn);
        }
        return null;
    }

    public JSONObject findBySQL(String sql, Object... data) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            if (data != null) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
            }
            rs = pstmt.executeQuery();
            logger.info("findBySQL: " + readOriginalSql(pstmt));
            return this.getJSONObject(rs);
        } catch (SQLException e) {
            logger.error("XModel.findBySQL.SQLException: " + sql, e);
        } finally {
            this.closeConnection(rs, pstmt, conn);
        }
        return null;
    }

    public JSONObject findBySQL(String sql) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            logger.info("findBySQL: " + readOriginalSql(pstmt));
            return this.getJSONObject(rs);
        } catch (SQLException e) {
            logger.error("XModel.findBySQL.SQLException: " + sql, e);
        } finally {
            this.closeConnection(rs, pstmt, conn);
        }
        return null;
    }

    public boolean executeByDDLSQL(String sql) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            int num = pstmt.executeUpdate();
            return (num == 0);
        } catch (SQLException e) {
            this.mException = e;
            logger.error("XModel.executeByDDLSQL.SQLException: " + sql, e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    public boolean executeBySQL(String sql, List<Object> data) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    pstmt.setObject(i + 1, data.get(i));
                }
            }
            int num = pstmt.executeUpdate();
            return (num > 0);
        } catch (SQLException e) {
            this.mException = e;
            logger.error("XModel.executeBySQL.SQLException: " + sql, e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    public boolean executeBySQL(String sql, Object... data) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            if (data != null) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
            }
            int num = pstmt.executeUpdate();
            return (num > 0);
        } catch (SQLException e) {
            this.mException = e;
            logger.error("XModel.executeBySQL.SQLException: " + sql, e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    public boolean executeBatchBySQL(String sql, List<List<Object>> data) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        try {
            if (isBadTransaction()) {
                conn.setAutoCommit(false);
            }
            pstmt = conn.prepareStatement(sql);
            List<Object> rowData = null;
            for (int i = 0; i < data.size(); i++) {
                rowData = data.get(i);
                for (int j = 0, m = rowData.size(); j < m; j++) {
                    pstmt.setObject(j + 1, rowData.get(j));
                }
                pstmt.addBatch();
            }
            int[] nums = pstmt.executeBatch();
            if (isBadTransaction()) {
                conn.commit();
                conn.setAutoCommit(true);
            }
            return (nums.length > 0);
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e2) {
                logger.error("XModel.insert.SQLException", e2);
            }
            this.mException = e;
            logger.error("XModel.executeBatchBySQL.SQLException: " + sql, e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    public JSONObject find() {
        Connection conn = this.getConnection();
        if (conn == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        prepareStatement(SQLType.SELECT);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            setStatementParams(pstmt);
            rs = pstmt.executeQuery();
            logger.info("find: " + readOriginalSql(pstmt));
            return this.getJSONObject(rs);
        } catch (SQLException e) {
            this.mException = e;
            logger.error("XModel.find.SQLException: " + mStatementSQL, e);
        } finally {
            this.closeConnection(rs, pstmt, conn);
        }
        return null;
    }

    public JSONArray select() {
        return this.select(false);
    }

    public JSONArray select(boolean single) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        prepareStatement(SQLType.SELECT);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            setStatementParams(pstmt);
            rs = pstmt.executeQuery();
            logger.info("select: " + readOriginalSql(pstmt));
            return this.getJSONArray(rs, single);
        } catch (SQLException e) {
            this.mException = e;
            logger.error("XModel.select.SQLException: " + readOriginalSql(pstmt), e);
        } finally {
            this.closeConnection(rs, pstmt, conn);
        }
        return null;
    }

    public JSONObject page(int pagenum, int pagesize) {
        return page(pagenum, pagesize, false);
    }

    public JSONObject page(int pagenum, int pagesize, boolean single) {
        this.limit((pagenum - 1) * pagesize, pagesize);
        Connection conn = this.getConnection();
        if (conn == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        prepareStatement(SQLType.COUNT);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            setStatementParams(pstmt);
            rs = pstmt.executeQuery();
            logger.info("pageCount: " + readOriginalSql(pstmt));

            JSONObject json = this.getJSONObject(rs);
            rs.close();
            pstmt.close();
            int total = json.getIntValue("total");

            JSONObject pageData = new JSONObject();
            pageData.put("total", total);
            pageData.put("pagenum", pagenum);
            pageData.put("pagesize", pagesize);
            JSONArray data = null;
            if (total > 0) {
                prepareStatement(SQLType.SELECT);
                pstmt = conn.prepareStatement(mStatementSQL);
                setStatementParams(pstmt);
                rs = pstmt.executeQuery();
                logger.info("page: " + readOriginalSql(pstmt));
                data = this.getJSONArray(rs, single);
            } else {
                data = new JSONArray();
            }
            pageData.put("data", data);
            return pageData;
        } catch (SQLException e) {
            logger.error("XModel.page.SQLException: " + mStatementSQL, e);
        } finally {
            this.closeConnection(rs, pstmt, conn);
        }
        return null;
    }

    public int count() {
        Connection conn = this.getConnection();
        if (conn == null) {
            return -1;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        prepareStatement(SQLType.COUNT);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            setStatementParams(pstmt);
            rs = pstmt.executeQuery();
            JSONObject json = this.getJSONObject(rs);
            return json.getIntValue("total");
        } catch (SQLException e) {
            logger.error("XModel.select.SQLException: " + mStatementSQL, e);
        } finally {
            this.closeConnection(rs, pstmt, conn);
        }
        return 0;
    }

    public boolean update() {
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        prepareStatement(SQLType.UPDATE);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            setStatementParams(pstmt);
            int num = pstmt.executeUpdate();
            logger.info("XModel.update: {}", readOriginalSql(pstmt));
            return (num > 0);
        } catch (SQLException e) {
            this.mException = e;
            logger.error("XModel.update.SQLException: " + mStatementSQL, e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    public boolean delete() {
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        prepareStatement(SQLType.DELETE);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            setStatementParams(pstmt);
            int num = pstmt.executeUpdate();
            return (num > 0);
        } catch (SQLException e) {
            this.mException = e;
            logger.error("XModel.delete.SQLException: " + mStatementSQL, e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    public boolean insert() {
        if (mValues.size() == 0) {
            return false;
        }
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        prepareStatement(SQLType.INSERT);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            for (int i = 0, n = mStatementParams.size(); i < n; i++) {
                pstmt.setObject(i + 1, mStatementParams.get(i));
            }
            int num = pstmt.executeUpdate();
            return (num > 0);
        } catch (SQLException e) {
            this.mException = e;
            logger.error("XModel.insert.SQLException: " + readOriginalSql(pstmt), e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    public long lastInsertId() {
        if (!this.isBadTransaction()) {
            Connection conn = this.getConnection();
            if (conn == null) {
                return 0;
            }
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                pstmt = conn.prepareStatement("SELECT LAST_INSERT_ID() AS `insertid`");
                rs = pstmt.executeQuery();
                JSONObject json = this.getJSONObject(rs);
                return json.getLongValue("insertid");
            } catch (SQLException e) {
                this.mException = e;
                logger.error("XModel.lastInsertId.SQLException", e);
            } finally {
                this.closeConnection(null, pstmt, conn);
            }
        }
        return 0;
    }

    public boolean replace() {
        if (mValues.size() == 0) {
            return false;
        }
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        prepareStatement(SQLType.REPLACE);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            for (int i = 0, n = mStatementParams.size(); i < n; i++) {
                pstmt.setObject(i + 1, mStatementParams.get(i));
            }
            int num = pstmt.executeUpdate();
            return (num > 0);
        } catch (SQLException e) {
            this.mException = e;
            logger.error("XModel.replace.SQLException: " + mStatementSQL, e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    public boolean insertUpdate() {
        if (mValues.size() == 0) {
            return false;
        }
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        prepareStatement(SQLType.INSERT_UPDATE);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            for (int i = 0, n = mStatementParams.size(); i < n; i++) {
                pstmt.setObject(i + 1, mStatementParams.get(i));
            }
            int num = pstmt.executeUpdate();
            logger.info("XModel.insertUpdate: {}", readOriginalSql(pstmt));
            return (num > 0);
        } catch (SQLException e) {
            this.mException = e;
            logger.error("XModel.insertUpdate.SQLException: " + readOriginalSql(pstmt), e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    public boolean insertBatch() {
        if (mBatchValues.size() == 0) {
            return false;
        }
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        prepareStatement(SQLType.INSERT_BATCH);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            if (isBadTransaction()) {
                conn.setAutoCommit(false);
            }
            List<Object> rowParams = null;
            for (int i = 0, n = mBatchStatementParams.size(); i < n; i++) {
                rowParams = mBatchStatementParams.get(i);
                for (int j = 0, m = rowParams.size(); j < m; j++) {
                    pstmt.setObject(j + 1, rowParams.get(j));
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            if (isBadTransaction()) {
                conn.commit();
                conn.setAutoCommit(true);
            }
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e2) {
                logger.error("XModel.insertBatch.SQLException", e2);
            }
            this.mException = e;
            logger.error("XModel.insertBatch.SQLException: " + mStatementSQL, e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    public boolean insertUpdateBatch() {
        if (mBatchValues.size() == 0) {
            return false;
        }
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        PreparedStatement pstmt = null;
        prepareStatement(SQLType.INSERT_UPDATE_BATCH);
        try {
            pstmt = conn.prepareStatement(mStatementSQL);
            if (isBadTransaction()) {
                conn.setAutoCommit(false);
            }
            List<Object> rowParams = null;
            for (int i = 0, n = mBatchStatementParams.size(); i < n; i++) {
                rowParams = mBatchStatementParams.get(i);
                for (int j = 0, m = rowParams.size(); j < m; j++) {
                    pstmt.setObject(j + 1, rowParams.get(j));
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            if (isBadTransaction()) {
                conn.commit();
                conn.setAutoCommit(true);
            }
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e2) {
                logger.error("XModel.insertUpdateBatch.SQLException", e2);
            }
            this.mException = e;
            logger.error("XModel.insertUpdateBatch.SQLException: " + mStatementSQL, e);
        } finally {
            this.closeConnection(null, pstmt, conn);
        }
        return false;
    }

    protected void setStatementParams(PreparedStatement pstmt) throws SQLException {
        int idx = 0;
        Object value = null;
        StringBuilder tempSB = new StringBuilder();
        for (int i = 0, n = mStatementParams.size(); i < n; i++) {
            value = mStatementParams.get(i);
            if (value instanceof JSONArray) {
                JSONArray values = (JSONArray) value;
                for (int j = 0, m = values.size(); j < m; j++) {
                    idx = idx + 1;
                    pstmt.setObject(idx, values.get(j));
                    tempSB.append(values.get(j) + ", ");
                }
            } else if (value instanceof Object[]) {
                Object[] values = (Object[]) value;
                for (int j = 0, m = values.length; j < m; j++) {
                    idx = idx + 1;
                    pstmt.setObject(idx, values[j]);
                    tempSB.append(values[j] + ", ");
                }
            } else {
                idx = idx + 1;
                pstmt.setObject(idx, value);
                tempSB.append(value + ", ");
            }
        }
        logger.info("XModel.StatementValues: " + tempSB.toString());
    }

    protected void prepareStatement(SQLType type) {
        StringBuilder sqlSB = new StringBuilder();
        mStatementParams.clear();
        if (type == SQLType.UPDATE) {
            sqlSB.append("UPDATE `" + tableName() + "` SET ");
            if (mValues.size() > 0) {
                ClauseValue clause = null;
                for (int i = 0, n = mValues.size(); i < n; i++) {
                    clause = mValues.get(i);
                    mStatementParams.add(clause.getValue());
                    if (i > 0) {
                        sqlSB.append(",");
                    }
                    sqlSB.append(clause.getPattern());
                }
            }
            sqlSB.append(" WHERE 1=1 ");
            if (mWheres.size() > 0) {
                ClauseWhere clause = null;
                Object value = null;
                for (int i = 0, n = mWheres.size(); i < n; i++) {
                    clause = mWheres.get(i);
                    value = clause.getValue();
                    if (value != null) {
                        mStatementParams.add(value);
                    }
                    sqlSB.append(" AND ");
                    sqlSB.append(clause.getPattern());
                }
            }
        } else if (type == SQLType.SELECT) {
            sqlSB.append("SELECT ");
            if (mFields.size() > 0) {
                if (mDistinct) {
                    sqlSB.append(" DISTINCT ");
                }
                String fieldstr = mFields.toString();
                sqlSB.append(fieldstr.substring(1, fieldstr.length() - 1));
            } else {
                sqlSB.append(" * ");
            }
            sqlSB.append(" FROM `" + tableName() + "` ");
            sqlSB.append(" WHERE 1=1 ");
            if (mWheres.size() > 0) {
                ClauseWhere clause = null;
                Object value = null;
                for (int i = 0, n = mWheres.size(); i < n; i++) {
                    clause = mWheres.get(i);
                    value = clause.getValue();
                    if (value != null) {
                        mStatementParams.add(value);
                    }
                    sqlSB.append(" AND ");
                    sqlSB.append(clause.getPattern());
                }
            }
            if (mGroup != null) {
                sqlSB.append(mGroup);
            }
            if (mOrderList.size() > 0) {
                StringBuilder orderSB = new StringBuilder();
                orderSB.append(" ORDER BY ");
                int num = 0;
                for (String name : mOrderList.keySet()) {
                    if (num > 0) {
                        orderSB.append(",");
                    }
                    orderSB.append(" `").append(name).append("` ");
                    if (mOrderList.get(name)) {
                        orderSB.append(" DESC ");
                    }
                    num++;
                }
                sqlSB.append(orderSB.toString());
            }
            if (mLimit != null) {
                sqlSB.append(mLimit);
            }
        } else if (type == SQLType.COUNT) {
            sqlSB.append("SELECT COUNT(1) AS total ");
            sqlSB.append(" FROM `" + tableName() + "` ");
            sqlSB.append(" WHERE 1=1 ");
            if (mWheres.size() > 0) {
                ClauseWhere clause = null;
                Object value = null;
                for (int i = 0, n = mWheres.size(); i < n; i++) {
                    clause = mWheres.get(i);
                    value = clause.getValue();
                    if (value != null) {
                        mStatementParams.add(value);
                    }
                    sqlSB.append(" AND ");
                    sqlSB.append(clause.getPattern());
                }
            }
        } else if (type == SQLType.INSERT) {
            sqlSB.append("INSERT INTO `" + tableName() + "` (");
            ClauseValue clause = null;
            StringBuilder patternSB = new StringBuilder();
            for (int i = 0, n = mValues.size(); i < n; i++) {
                clause = mValues.get(i);
                mStatementParams.add(clause.getValue());
                if (i > 0) {
                    sqlSB.append(",");
                    patternSB.append(",");
                }
                patternSB.append("?");
                sqlSB.append(clause.getKey());
            }
            sqlSB.append(") VALUES (" + patternSB.toString() + ")");
        } else if (type == SQLType.REPLACE) {
            sqlSB.append("REPLACE INTO `" + tableName() + "` (");
            ClauseValue clause = null;
            StringBuilder patternSB = new StringBuilder();
            for (int i = 0, n = mValues.size(); i < n; i++) {
                clause = mValues.get(i);
                mStatementParams.add(clause.getValue());
                if (i > 0) {
                    sqlSB.append(",");
                    patternSB.append(",");
                }
                patternSB.append("?");
                sqlSB.append(clause.getKey());
            }
            sqlSB.append(") VALUES (" + patternSB.toString() + ")");
        } else if (type == SQLType.INSERT_UPDATE) {
            sqlSB.append("INSERT INTO `" + tableName() + "` (");
            ClauseValue clause = null;
            StringBuilder patternSB = new StringBuilder();
            for (int i = 0, n = mValues.size(); i < n; i++) {
                clause = mValues.get(i);
                mStatementParams.add(clause.getValue());
                if (i > 0) {
                    sqlSB.append(",");
                    patternSB.append(",");
                }
                patternSB.append("?");
                sqlSB.append(clause.getKey());
            }
            sqlSB.append(") VALUES (" + patternSB.toString() + ")");
            sqlSB.append(" ON DUPLICATE KEY UPDATE ");
            String updateField = null;
            ClauseValue upClause = null;
            Object upField = null;
            if (mUpdateFields.size() > 0) {
                for (int i = 0, n = mUpdateFields.size(); i < n; i++) {
                    if (i > 0) {
                        sqlSB.append(",");
                    }
                    upField = mUpdateFields.get(i);
                    if (upField instanceof ClauseValue) {
                        upClause = (ClauseValue) upField;
                        updateField = upClause.getKey();
                        sqlSB.append(updateField).append("=").append(updateField);
                        sqlSB.append(upClause.calc == ValueCalc.add ? "+?" : upClause.calc == ValueCalc.sub ? "-?" : upClause.calc == ValueCalc.mul ? "*?" : "/?");
                        mStatementParams.add(upClause.getValue());
                    } else if (upField instanceof String) {
                        updateField = "`" + upField + "`";
                        sqlSB.append(updateField).append("=VALUES(").append(updateField).append(")");
                    }
                }
            } else {
                for (int i = 0, n = mValues.size(); i < n; i++) {
                    clause = mValues.get(i);
                    if (i > 0) {
                        sqlSB.append(",");
                    }
                    sqlSB.append(clause.getKey()).append("=VALUES(").append(clause.getKey()).append(")");
                }
            }
        } else if (type == SQLType.INSERT_BATCH) {
            sqlSB.append("INSERT INTO `" + tableName() + "` (");
            ClauseValue clause = null;
            List<ClauseValue> values = null;
            List<Object> params = null;
            StringBuilder patternSB = new StringBuilder();
            for (int i = 0, n = mBatchValues.size(); i < n; i++) {
                values = mBatchValues.get(i);
                params = new ArrayList<Object>();
                if (i == 0) {
                    for (int j = 0, m = values.size(); j < m; j++) {
                        clause = values.get(j);
                        params.add(clause.getValue());
                        if (j > 0) {
                            sqlSB.append(",");
                            patternSB.append(",");
                        }
                        patternSB.append("?");
                        sqlSB.append(clause.getKey());
                    }
                } else {
                    for (int j = 0, m = values.size(); j < m; j++) {
                        clause = values.get(j);
                        params.add(clause.getValue());
                    }
                }
                mBatchStatementParams.add(params);
            }
            sqlSB.append(") VALUES (" + patternSB.toString() + ")");
        } else if (type == SQLType.INSERT_UPDATE_BATCH) {
            sqlSB.append("INSERT INTO `" + tableName() + "` (");
            ClauseValue clause = null;
            List<ClauseValue> values = null;
            List<Object> params = null;
            StringBuilder patternSB = new StringBuilder();
            for (int i = 0, n = mBatchValues.size(); i < n; i++) {
                values = mBatchValues.get(i);
                params = new ArrayList<Object>();
                if (i == 0) {
                    for (int j = 0, m = values.size(); j < m; j++) {
                        clause = values.get(j);
                        params.add(clause.getValue());
                        if (j > 0) {
                            sqlSB.append(",");
                            patternSB.append(",");
                        }
                        patternSB.append("?");
                        sqlSB.append(clause.getKey());
                    }
                } else {
                    for (int j = 0, m = values.size(); j < m; j++) {
                        clause = values.get(j);
                        params.add(clause.getValue());
                    }
                }
                mBatchStatementParams.add(params);
            }
            sqlSB.append(") VALUES (" + patternSB.toString() + ")");
            sqlSB.append(" ON DUPLICATE KEY UPDATE ");
            String updateField = null;
            if (mUpdateFields.size() > 0) {
                for (int i = 0, n = mUpdateFields.size(); i < n; i++) {
                    updateField = "`" + mUpdateFields.get(i) + "`";
                    if (i > 0) {
                        sqlSB.append(",");
                    }
                    sqlSB.append(updateField).append("=VALUES(").append(updateField).append(")");
                }
            } else {
                mValues = mBatchValues.get(0);
                for (int i = 0, n = mValues.size(); i < n; i++) {
                    clause = mValues.get(i);
                    if (i > 0) {
                        sqlSB.append(",");
                    }
                    sqlSB.append(clause.getKey()).append("=VALUES(").append(clause.getKey()).append(")");
                }
            }
        } else if (type == SQLType.DELETE) {
            sqlSB.append("DELETE FROM `" + tableName() + "` ");
            sqlSB.append(" WHERE 1=1 ");
            if (mWheres.size() > 0) {
                ClauseWhere clause = null;
                Object value = null;
                for (int i = 0, n = mWheres.size(); i < n; i++) {
                    clause = mWheres.get(i);
                    value = clause.getValue();
                    if (value != null) {
                        mStatementParams.add(value);
                    }
                    sqlSB.append(" AND ");
                    sqlSB.append(clause.getPattern());
                }
            }
        }
        mStatementSQL = sqlSB.toString();
        logger.info("XModel.StatementSQL: " + mStatementSQL);
    }

    protected JSONObject getJSONObject(ResultSet rs) throws SQLException {
        JSONObject data = new JSONObject();
        if (rs == null) {
            return data;
        }

        if (!rs.next()) {
            return data;
        }

        ResultSetMetaData metaData = rs.getMetaData();
        int index = 0, type, count = metaData.getColumnCount();
        for (index = 1; index <= count; index++) {
            type = metaData.getColumnType(index);
            if (Types.INTEGER == type || Types.TINYINT == type || Types.SMALLINT == type || Types.BIT == type) {
                data.put(metaData.getColumnLabel(index), rs.getInt(index));
            } else if (Types.BIGINT == type) {
                data.put(metaData.getColumnLabel(index), rs.getLong(index));
            } else if (Types.DOUBLE == type || Types.FLOAT == type || Types.DECIMAL == type) {
                data.put(metaData.getColumnLabel(index), rs.getDouble(index));
            } else {
                data.put(metaData.getColumnLabel(index), rs.getString(index));
            }
        }
        rs.close();
        return data;
    }

    protected JSONArray getJSONArray(ResultSet rs) throws SQLException {
        return this.getJSONArray(rs, false);
    }

    protected JSONArray getJSONArray(ResultSet rs, boolean single) throws SQLException {
        JSONArray data = new JSONArray();
        if (rs == null) {
            return data;
        }

        ResultSetMetaData metaData = rs.getMetaData();
        int index = 0, type, count = metaData.getColumnCount();
        while (rs.next()) {
            if (single && count == 1) {
                type = metaData.getColumnType(1);
                if (Types.INTEGER == type || Types.TINYINT == type || Types.SMALLINT == type || Types.BIT == type) {
                    data.add(rs.getInt(1));
                } else if (Types.BIGINT == type) {
                    data.add(rs.getLong(1));
                } else if (Types.DOUBLE == type || Types.FLOAT == type || Types.DECIMAL == type) {
                    data.add(rs.getDouble(1));
                } else {
                    data.add(rs.getString(1));
                }
            } else {
                JSONObject item = new JSONObject();
                for (index = 1; index <= count; index++) {
                    type = metaData.getColumnType(index);
                    if (Types.INTEGER == type || Types.TINYINT == type || Types.SMALLINT == type || Types.BIT == type) {
                        item.put(metaData.getColumnLabel(index), rs.getInt(index));
                    } else if (Types.BIGINT == type) {
                        item.put(metaData.getColumnLabel(index), rs.getLong(index));
                    } else if (Types.DOUBLE == type || Types.FLOAT == type || Types.DECIMAL == type) {
                        item.put(metaData.getColumnLabel(index), rs.getDouble(index));
                    } else {
                        item.put(metaData.getColumnLabel(index), rs.getString(index));
                    }
                }
                data.add(item);
            }
        }
        rs.close();
        return data;
    }

    public XModel set(String key, Object value) {
        mValues.add(new ClauseValue(key.trim(), value));
        return this;
    }

    public XModel setx(String key, Object value, ValueCalc calc) {
        mValues.add(new ClauseValue(key.trim(), value, calc));
        return this;
    }

    public XModel setUpdateFields(String... fields) {
        mUpdateFields.addAll(Arrays.asList(fields));
        return this;
    }

    public XModel setUpdateField(String field, Object valus, ValueCalc calc) {
        mUpdateFields.add(new ClauseValue(field, valus, calc));
        return this;
    }

    public XModel set(Map<String, Object> data) {
        for (String key : data.keySet()) {
            mValues.add(new ClauseValue(key.trim(), data.get(key)));
        }
        return this;
    }

    public XModel set(List<Map<String, Object>> data) {
        Map<String, Object> row = null;
        List<ClauseValue> tempValues = null;
        for (int i = 0, n = data.size(); i < n; i++) {
            row = data.get(i);
            tempValues = new ArrayList<ClauseValue>();
            for (String key : row.keySet()) {
                tempValues.add(new ClauseValue(key.trim(), row.get(key)));
            }
            mBatchValues.add(tempValues);
        }

        return this;
    }

    public XModel addWhere(String key) {
        mWheres.add(new ClauseWhere(key));
        return this;
    }

    public XModel addWhere(String key, Object value) {
        mWheres.add(new ClauseWhere(key.trim(), value));
        return this;
    }

    public XModel addWhere(String key, Object value, WhereType type) {
        if (type == WhereType.IN) {
            if (value instanceof JSONArray || value instanceof Object[]) {
                mWheres.add(new ClauseWhere(key.trim(), value, type));
            } else {
                logger.error("SQLError:WHERE[IN] value is not instanceof Object[], key=" + key);
            }
        } else {
            mWheres.add(new ClauseWhere(key.trim(), value, type));
        }
        return this;
    }

    public XModel addWhere(ClauseWhere where) {
        mWheres.add(where);
        return this;
    }

    public XModel addWhere(List<ClauseWhere> wheres) {
        mWheres.addAll(wheres);
        return this;
    }

    public XModel addWhere(Map<String, Object> wheres) {
        for (String key : wheres.keySet()) {
            mWheres.add(new ClauseWhere(key.trim(), wheres.get(key)));
        }
        return this;
    }

    public XModel order(String field, boolean desc) {
        mOrderList.put(field, desc);
        return this;
    }

    public XModel group(String... fields) {
        if (fields.length > 0) {
            StringBuilder fieldSB = new StringBuilder();
            fieldSB.append(" GROUP BY ");
            for (int i = 0, n = fields.length; i < n; i++) {
                if (i > 0) {
                    fieldSB.append(",");
                }
                fieldSB.append("`" + fields[i].trim() + "`");
            }
            mGroup = fieldSB.toString();
        }

        return this;
    }

    public XModel distinct(boolean flag) {
        mDistinct = flag;
        return this;
    }

    public XModel limit(int offset, int total) {
        mLimit = " LIMIT " + offset + "," + total;
        return this;
    }

    public XModel limit(int total) {
        mLimit = " LIMIT " + total;
        return this;
    }

    public XModel field(String fields) {
        if (fields != null) {
            return this.field(fields.split(","));
        }
        return this;
    }

    public XModel field(String[] fields) {
        if (fields != null) {
            String field = null;
            for (int i = 0, n = fields.length; i < n; i++) {
                field = fields[i].trim();
                if (field.indexOf("`") >= 0) {
                    mFields.add(field);
                } else {
                    if (field.indexOf(" ") > 0 || field.indexOf("(") >= 0) {
                        mFields.add(field);
                    } else {
                        mFields.add("`" + field + "`");
                    }
                }
            }
        }
        return this;
    }

    public XModel prepare() {
        mFields.clear();
        mWheres.clear();
        mValues.clear();
        mUpdateFields.clear();
        mBatchValues.clear();
        mOrderList.clear();
        mGroup = null;
        mLimit = null;
        mDistinct = false;
        mStatementSQL = null;
        mException = null;
        mStatementParams.clear();
        mBatchStatementParams.clear();

        return this;
    }

    public enum SQLType {INSERT, INSERT_BATCH, INSERT_UPDATE, INSERT_UPDATE_BATCH, REPLACE, SELECT, COUNT, UPDATE, DELETE}

    ;

    public enum WhereType {EQ, NEQ, IN, GT, LT, GET, LET, LIKE, LLIKE, RLIKE}

    ;

    public class ClauseWhere {

        private String key = null;
        private Object value = null;
        private WhereType type = null;

        public ClauseWhere(String key) {
            this.key = key;
            this.value = null;
        }

        public ClauseWhere(String key, Object value) {
            this.key = key.trim();
            this.value = value;
            this.type = WhereType.EQ;
        }

        public ClauseWhere(String key, Object value, WhereType type) {
            this.key = key.trim();
            this.value = value;
            this.type = type;
        }

        private String getInPattern() {
            StringBuilder sb = new StringBuilder();
            if (value instanceof JSONArray) {
                JSONArray values = (JSONArray) this.value;
                for (int i = 0, n = values.size(); i < n; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("?");
                }
            } else if (value instanceof Object[]) {
                Object[] values = (Object[]) this.value;
                for (int i = 0, n = values.length; i < n; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("?");
                }
            }
            return sb.toString();
        }

        public Object getValue() {
            if (this.type != null) {
                switch (type) {
                    case LIKE:
                        return "%" + this.value + "%";
                    case LLIKE:
                        return "%" + this.value;
                    case RLIKE:
                        return this.value + "%";
                    default:
                        return this.value;
                }
            }
            return this.value;
        }

        public String getPattern() {
            if (this.type != null) {
                switch (type) {
                    case EQ:
                        return " (`" + this.key + "`=?) ";
                    case NEQ:
                        return " (`" + this.key + "`<>?) ";
                    case IN:
                        return " (`" + this.key + "` IN (" + getInPattern() + ")) ";
                    case LT:
                        return " (`" + this.key + "`<?) ";
                    case GT:
                        return " (`" + this.key + "`>?) ";
                    case LET:
                        return " (`" + this.key + "`<=?) ";
                    case GET:
                        return " (`" + this.key + "`>=?) ";
                    case LIKE:
                        return " (`" + this.key + "` LIKE ?) ";
                    case LLIKE:
                        return " (`" + this.key + "` LIKE ?) ";
                    case RLIKE:
                        return " (`" + this.key + "` LIKE ?) ";
                }
            }
            if (this.value == null) {
                return " (" + this.key + ") ";
            }
            return " (`" + this.key + "`=?) ";
        }

    }

    public enum ValueCalc {add, sub, mul, div}

    ;

    public class ClauseValue {

        private String key = null;
        private Object value = null;
        private ValueCalc calc = null;

        public ClauseValue(String key, Object value) {
            this.key = key.trim();
            this.value = value;
        }

        public ClauseValue(String key, Object value, ValueCalc calc) {
            this.key = key.trim();
            this.value = value;
            this.calc = calc;
        }

        public Object getValue() {
            return this.value;
        }

        public String getPattern() {
            if (calc != null) {
                switch (calc) {
                    case add:
                        return " `" + this.key + "`=(`" + this.key + "`+?) ";
                    case sub:
                        return " `" + this.key + "`=(`" + this.key + "`-?) ";
                    case mul:
                        return " `" + this.key + "`=(`" + this.key + "`*?) ";
                    case div:
                        return " `" + this.key + "`=(`" + this.key + "`/?) ";
                }
            }
            return " `" + this.key + "`=? ";
        }

        public String getKey() {
            return "`" + this.key + "`";
        }
    }

    public boolean isErrorDuplicateEntry() {
        if (this.mException != null) {
            logger.error("XModel.SQLExcpetion:ErrorCode=" + this.mException.getErrorCode() + ", SQLSTATE=" + this.mException.getSQLState());
            return 1062 == this.mException.getErrorCode();
        }
        return false;
    }

    public int getErrorCode() {
        if (this.mException != null) {
            logger.error("XModel.SQLExcpetion:ErrorCode=" + this.mException.getErrorCode() + ", SQLSTATE=" + this.mException.getSQLState());
            return this.mException.getErrorCode();
        }
        return 1;
    }

    public String getErrorMessage() {
        if (this.mException != null) {
            logger.error("XModel.SQLExcpetion:ErrorCode=" + this.mException.getErrorCode() + ", SQLSTATE=" + this.mException.getSQLState());
            return this.mException.getMessage();
        }
        return "";
    }

    public String formatFields(String fields) {
        return this.formatFields(fields, null);
    }

    public String formatFields(String fields, String tableAlias) {
        if (fields != null) {
            String prefix = "";
            if (tableAlias != null && !tableAlias.isEmpty()) {
                prefix = tableAlias + ".";
            }
            StringBuilder fieldSB = new StringBuilder();
            String[] strArr = fields.split(",");
            for (int i = 0, n = strArr.length; i < n; i++) {
                fieldSB.append(prefix).append("`" + strArr[i].trim() + "`");
                if (i < strArr.length - 1) {
                    fieldSB.append(",");
                }
            }
            return fieldSB.toString();
        }
        return null;
    }

    protected String formatInValues(Object[] values) {
        if (values != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0, n = values.length; i < n; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("?");
            }
            sb.append(")");
            return sb.toString();
        }
        return null;
    }

    protected String formatInValues(JSONArray values) {
        if (values != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0, n = values.size(); i < n; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("?");
            }
            sb.append(")");
            return sb.toString();
        }
        return null;
    }

    protected SQLBuilder newSQLBuilder() {
        return new SQLBuilder(this);
    }

    public class SQLBuilder {
        private XModel model = null;
        private StringBuilder sqlSB = null;

        public SQLBuilder(XModel model) {
            this.model = model;
            sqlSB = new StringBuilder();
        }

        public SQLBuilder appendField(String fields) {
            sqlSB.append(this.model.formatFields(fields));
            return this;
        }

        public SQLBuilder fromTable() {
            sqlSB.append(" FROM `").append(this.model.tableName()).append("` ");
            return this;
        }

        public SQLBuilder fromTable(String tableName) {
            sqlSB.append(" FROM `").append(tableName).append("` ");
            return this;
        }

        public SQLBuilder appendTable() {
            sqlSB.append(" `").append(this.model.tableName()).append("` ");
            return this;
        }

        public SQLBuilder appendTable(String tableName) {
            sqlSB.append(" `").append(tableName).append("` ");
            return this;
        }

        public SQLBuilder appendWhere(String conditions) {
            sqlSB.append(" AND ").append(conditions).append(" ");
            return this;
        }

        public SQLBuilder append(Object sql) {
            sqlSB.append(String.valueOf(sql));
            return this;
        }

        @Override
        public String toString() {
            String sql = sqlSB.toString();
            if (sqlSB.length() > 0) {
                this.clear();
            }
            return sql;
        }

        public SQLBuilder clear() {
            sqlSB.setLength(0);
            return this;
        }

    }

}

