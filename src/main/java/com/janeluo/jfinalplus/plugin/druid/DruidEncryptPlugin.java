/**
 * Copyright (c) 2015-2016, BruceZCQ (zcq@zhucongqi.cn).
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
package com.janeluo.jfinalplus.plugin.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.jfinal.log.Log;
import com.jfinal.plugin.druid.DruidPlugin;

import java.sql.SQLException;

/**
 * 生成加密密码:
 * java -cp druid-xx.jar com.alibaba.druid.filter.config.ConfigTools your_password
 *
 * @author BruceZCQ
 */
public class DruidEncryptPlugin extends DruidPlugin {
    private final Log log = Log.getLog(getClass());

    /**
     * @param url
     * @param username
     * @param password
     */
    public DruidEncryptPlugin(String url, String username, String password) {
        super(url, username, password);
    }

    /**
     * @param url
     * @param username
     * @param password
     * @param driverClass
     */
    public DruidEncryptPlugin(String url, String username, String password, String driverClass) {
        super(url, username, password, driverClass);
    }

    /**
     * @param url
     * @param username
     * @param password
     * @param driverClass
     * @param filters
     */
    public DruidEncryptPlugin(String url, String username, String password, String driverClass, String filters) {
        super(url, username, password, driverClass, filters);
    }

    @Override
    public boolean start() {
        boolean ret = super.start();
        DruidDataSource ds = (DruidDataSource) this.getDataSource();
        ds.setConnectionProperties("config.decrypt=true");
        try {
            ds.setFilters("config");
        } catch (SQLException e1) {
            log.error("连接池启动失败", e1);
        }
        return ret;
    }
}
