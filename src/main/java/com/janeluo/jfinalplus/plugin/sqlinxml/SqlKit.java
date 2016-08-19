/**
 * Copyright (c) 2011-2013, kidzhou 周磊 (zhouleib1412@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.janeluo.jfinalplus.plugin.sqlinxml;

import com.janeluo.jfinalplus.kit.JaxbKit;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

public class SqlKit {

    protected static final Log LOG = Log.getLog(SqlKit.class);

    private static Map<String, String> sqlMap;

    public static String sql(String groupNameAndsqlId) {
        if (sqlMap == null) {
            throw new NullPointerException("SqlInXmlPlugin not start");
        }
        return sqlMap.get(groupNameAndsqlId);
    }

    static void clearSqlMap() {
        sqlMap.clear();
    }

    static void init() {
        sqlMap = new HashMap<String, String>();
        File file = new File(PathKit.getRootClassPath());
        findSqlFile(file);
        LOG.debug("sqlMap" + sqlMap);
    }

    private static void findSqlFile(File file) {

        // 过滤出当前目录下所有的sql文件
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith("sql.xml")) {
                    return true;
                }
                return false;
            }
        });
        for (File xmlfile : files) {
            SqlGroup group = JaxbKit.unmarshal(xmlfile, SqlGroup.class);
            String name = group.name;
            if (name == null || name.trim().equals("")) {
                name = xmlfile.getName();
            }
            for (SqlItem sqlItem : group.sqlItems) {
                String sqlkey = name + "." + sqlItem.id;
                if(sqlMap.containsKey(sqlkey)) {
                    throw new RuntimeException(sqlkey + "In other file already exists");
                }
                sqlMap.put(sqlkey, sqlItem.value);
            }
        }

        // 过滤出当前目录下所有的文件夹
        File[] folders = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isFile();
            }
        });

        // 递归下探寻找sql文件
        for(File folder : folders){
            findSqlFile(folder);
        }
    }
}
