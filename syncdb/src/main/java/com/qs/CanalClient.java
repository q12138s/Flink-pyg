package com.qs;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * @author:qisuhai
 * @date:2019/11/21
 * @description:
 */
public class CanalClient {
    public static void main(String[] args) {
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("hadoop02", 11111), "example", "root", "mysql");
        int batchSize = 1;
        int emtyCount = 1;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");//订阅所有的库、所有的表
            connector.rollback();
            int totalCount = 100;
            while (totalCount > emtyCount) {
                Message message = connector.getWithoutAck(batchSize);
                long batchID = message.getId();
                int size = message.getEntries().size();

                if (batchID != -1L || size != 0) {
                    System.out.println("<<<<<<<<<<<<<<<<<<<<<");
                    Analysis(message.getEntries(), emtyCount);
                    ++emtyCount;
                } else {
//                    System.out.println("===============无数据=================");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connector.disconnect();
        }
    }

    public static void Analysis(List<Entry> entries, int emptyCount) {
        for (Entry entry : entries) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN) continue;
            if (entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }
            RowChange rowChange = null;
            try {
                rowChange = RowChange.parseFrom(entry.getStoreValue());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }

            //获取关键字段
            final EventType eventType = rowChange.getEventType();
            final String logfileName = entry.getHeader().getLogfileName();
            final long logfileOffset = entry.getHeader().getLogfileOffset();
            final String dbname = entry.getHeader().getSchemaName();
            final String tableName = entry.getHeader().getTableName();
            long timestamp = entry.getHeader().getExecuteTime();//执行时间

            for (RowData rowData : rowChange.getRowDatasList()) {

                System.out.println("<<<<<<<<<<<<<logfileName:" + logfileName + ",logfileOffset:" + logfileOffset
                        + ",dbname:" + dbname + ",rowData:" + rowData.toString());
                if (eventType == EventType.DELETE)
                    dataDetails(rowData.getBeforeColumnsList(), logfileName, logfileOffset, dbname, tableName, eventType, emptyCount, timestamp);
                else if (eventType == EventType.INSERT)
                    dataDetails(rowData.getAfterColumnsList(), logfileName, logfileOffset, dbname, tableName, eventType, emptyCount, timestamp);
                else
                    dataDetails(rowData.getAfterColumnsList(), logfileName, logfileOffset, dbname, tableName, eventType, emptyCount, timestamp);
            }
        }

    }

    public static void dataDetails(List<Column> columns, String logfileName, long logfileOffset, String dbname, String tableName, EventType eventType, int emptyCount, long timestamp) {

        // 找到当前那些列发生了改变  以及改变的值
        List<ColumnValuePair> columnValueList = new ArrayList<ColumnValuePair>();

        for (Column column : columns) {
            ColumnValuePair columnValuePair = new ColumnValuePair(column.getName(), column.getValue(), column.getUpdated());
            columnValueList.add(columnValuePair);
        }

        String data = logfileName + "##" + logfileOffset + "##" + dbname + "##" + tableName + "##" + eventType + "##" + columnValueList + "##" + emptyCount;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("logFileName", logfileName);
        jsonObject.put("logFileOffset", logfileOffset);
        jsonObject.put("dbName", dbname);
        jsonObject.put("tableName", tableName);
        jsonObject.put("eventType", eventType);
        jsonObject.put("columnValueList", columnValueList);
        jsonObject.put("emptyCount", emptyCount);
        jsonObject.put("timestamp", timestamp);

        // 拼接所有binlog解析的字段
        String jsonStr = JSON.toJSONString(jsonObject);

        System.out.println(jsonStr);
        KafkaUtil.sendData("mycanal", jsonStr);
    }

    static class ColumnValuePair {
        private String columnName;
        private String columnValue;
        private Boolean isValid;

        public ColumnValuePair(String columnName, String columnValue, Boolean isValid) {
            this.columnName = columnName;
            this.columnValue = columnValue;
            this.isValid = isValid;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnValue() {
            return columnValue;
        }

        public void setColumnValue(String columnValue) {
            this.columnValue = columnValue;
        }

        public Boolean getIsValid() {
            return isValid;
        }

        public void setIsValid(Boolean isValid) {
            this.isValid = isValid;
        }
    }

}
