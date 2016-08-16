package com.janeluo.jfinalplus;


import com.janeluo.jfinalplus.config.JFinalConfigExt;
import com.jfinal.config.*;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Generate Tool
 * @author BruceZCQ
 */
public class Ge {

	/**
	 * Generate the models and base models
	 * @param args
	 */
	public static void main(String[] args) {
        JFinalConfig jfinalConfig = new JFinalConfigExt() {
            @Override
            public void configMoreConstants(Constants me) {

            }

            @Override
            public void configMoreRoutes(Routes me) {

            }

            @Override
            public void configMorePlugins(Plugins me) {

            }

            @Override
            public void configTablesMapping(String configName, ActiveRecordPlugin arp) {

            }

            @Override
            public void configMoreInterceptors(Interceptors me) {

            }

            @Override
            public void configMoreHandlers(Handlers me) {

            }

            @Override
            public void afterJFinalStarted() {

            }
        };
        Config.configJFinal(jfinalConfig);	// start plugin and init log factory in this method
	}

}
