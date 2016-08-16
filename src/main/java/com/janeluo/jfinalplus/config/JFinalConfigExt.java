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
package com.janeluo.jfinalplus.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.janeluo.jfinalplus.filerenamepolicy.RandomFileRenamePolicy;
import com.janeluo.jfinalplus.handler.ActionExtentionHandler;
import com.janeluo.jfinalplus.interceptor.NotFoundActionInterceptor;
import com.janeluo.jfinalplus.interceptor.OnExceptionInterceptorExt;
import com.janeluo.jfinalplus.kit.PageViewKit;
import com.janeluo.jfinalplus.plugin.activerecord.generator.MappingKitGeneratorExt;
import com.janeluo.jfinalplus.plugin.activerecord.generator.ModelGeneratorExt;
import com.janeluo.jfinalplus.route.AutoBindRoutes;
import com.jfinal.config.*;
import com.jfinal.core.Const;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.generator.BaseModelGenerator;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.ViewType;
import com.jfinal.upload.OreillyCos;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author BruceZCQ
 */
public abstract class JFinalConfigExt extends com.jfinal.config.JFinalConfig {

    private static final String ACTIVE_TEMPLATE = "db.%s.active";
    private static final String URL_TEMPLATE = "jdbc:%s://%s";
    private static final String USER_TEMPLATE = "db.%s.user";
    private static final String PASSWORD_TEMPLATE = "db.%s.password";
    private static final String INITSIZE_TEMPLATE = "db.%s.initsize";
    private static final String MAXSIZE_TEMPLATE = "db.%s.maxactive";


    public static String APP_NAME = null;
    protected boolean geRuned = false;

    public String configFileName = "config.txt";


    /**
     * Config other More constant
     *
     * @param me Constants
     */
    public abstract void configMoreConstants(Constants me);

    /**
     * Config other more route
     *
     * @param me 已配置规则
     */
    public abstract void configMoreRoutes(Routes me);

    /**
     * Config other more plugin
     *
     * @param me 已配置插件
     */
    public abstract void configMorePlugins(Plugins me);

    /**
     * Config other Tables Mapping
     *
     * @param configName 名称
     * @param arp        ActiveRecordPlugin
     */
    public abstract void configTablesMapping(String configName, ActiveRecordPlugin arp);

    /**
     * Config other more interceptor applied to all actions.
     *
     * @param me Interceptors
     */
    public abstract void configMoreInterceptors(Interceptors me);

    /**
     * Config other more handler
     *
     * @param me Handlers
     */
    public abstract void configMoreHandlers(Handlers me);

    /**
     * After JFinalStarted
     */
    public abstract void afterJFinalStarted();

    /**
     * Config constant
     *
     * @param me Constants
     *           <p>
     *           Default <br>
     *           ViewType: JSP <br>
     *           Encoding: UTF-8 <br>
     *           ErrorPages: <br>
     *           404 : /WEB-INF/errorpages/404.jsp <br>
     *           500 : /WEB-INF/errorpages/500.jsp <br>
     *           403 : /WEB-INF/errorpages/403.jsp <br>
     *           UploadedFileSaveDirectory : cfg basedir + appName <br>
     */
    @Override
    public void configConstant(Constants me) {

//        File file = new File(configFileName);
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName);

        if (resourceAsStream != null) {
            PropKit.use(configFileName); // 加载少量必要配置，随后可用PropKit.get(...)获取值
            me.setDevMode(this.getAppDevMode());
            //file upload dir
            me.setBaseUploadPath(this.getUploadPath());
            //file download dir
            me.setBaseDownloadPath(this.getDownloadPath());
            JFinalConfigExt.APP_NAME = this.getAppName();
        }

        resourceAsStream = null;

        me.setViewType(ViewType.JSP);

        me.setEncoding(Const.DEFAULT_ENCODING);
        me.setError404View(PageViewKit.get404PageView());
        me.setError500View(PageViewKit.get500PageView());
        me.setError403View(PageViewKit.get403PageView());
        //set file rename policy is random
        OreillyCos.setFileRenamePolicy(new RandomFileRenamePolicy());

        // config others
        configMoreConstants(me);
    }

    /**
     * Config route
     * Config the AutoBindRoutes
     * <pre>
     * 自动bindRoute。controller命名为xxController。<br>
     * AutoBindRoutes自动取xxController对应的class的Controller之前的xx作为controllerKey(path)<br>
     * 如：MyUserController => myuser; UserController => user; UseradminController => useradmin<br>
     * </pre>
     *
     * @param me Routes
     */
    public void configRoute(Routes me) {
        me.add(new AutoBindRoutes());
        // config others
        configMoreRoutes(me);
    }

    /**
     * Config plugin
     *
     * @param me Plugins
     */
    public void configPlugin(Plugins me) {
        String[] dses = this.getDataSource();
        for (String ds : dses) {
            if (!this.getDbActiveState(ds)) {
                continue;
            }
            DruidPlugin drp = this.getDruidPlugin(ds);
            me.add(drp);
            ActiveRecordPlugin arp = this.getActiveRecordPlugin(ds, drp);
            me.add(arp);
            configTablesMapping(ds, arp);
        }
        // config others
        configMorePlugins(me);
    }


    /**
     * Config interceptor applied to all actions.
     *
     * @param me Interceptors
     */
    public void configInterceptor(Interceptors me) {
        // when action not found fire 404 error
        me.add(new NotFoundActionInterceptor());
        // add excetion interceptor
        me.add(new OnExceptionInterceptorExt());
        if (this.getHttpPostMethod()) {
            me.add(new POST());
        }
        // config others
        configMoreInterceptors(me);
    }

    /**
     * Config handler
     */
    public void configHandler(Handlers me) {
        // add extension handler
        me.add(new ActionExtentionHandler());
        // config others
        configMoreHandlers(me);
    }

    public void afterJFinalStart() {
        super.afterJFinalStart();
        this.afterJFinalStarted();
    }


    private boolean getHttpPostMethod() {
        return PropKit.getBoolean("app.post", false);
    }

    private String getPath(String property) {
        if (StrKit.isBlank(property) || (!"downloads".equals(property) && !"uploads".equals(property))) {
            throw new IllegalArgumentException("property is invalid, property just use `downloads` or `uploads`");
        }
        String app = this.getAppName();
        String baseDir = PropKit.get(String.format("app.%s.basedir", property));
        if (baseDir.endsWith("/")) {
            if (!baseDir.endsWith(property + "/")) {
                baseDir += (property + "/");
            }
        } else {
            if (!baseDir.endsWith(property)) {
                baseDir += ("/" + property + "/");
            } else {
                baseDir += "/";
            }
        }
        return (new StringBuilder(baseDir).append(app).toString());
    }

    /**
     * 获取File Upload Directory
     * "/var/uploads/appname"
     *
     * @return 上传文件目录
     */
    private String getUploadPath() {
        return this.getPath("uploads");
    }

    /**
     * 获取File Download Directory
     * "/var/downloads/appname"
     *
     * @return
     */
    private String getDownloadPath() {
        return this.getPath("downloads");
    }

    /**
     * 获取app的dev mode
     *
     * @return
     */
    private boolean getAppDevMode() {
        return PropKit.getBoolean("app.dev", true);
    }

    /**
     * 获取 AppName
     *
     * @return
     */
    private String getAppName() {
        String appName = PropKit.get("app.name", "");
        if (StrKit.isBlank(appName)) {
            throw new IllegalArgumentException("Please Set Your App Name in Your cfg file");
        }
        return appName;
    }


    /**
     * 获取是否打开数据库状态
     *
     * @return
     */
    private boolean getDbActiveState(String ds) {
        return PropKit.getBoolean(String.format(ACTIVE_TEMPLATE, ds), false);
    }

    /**
     * 获取数据源
     *
     * @return
     */
    private String[] getDataSource() {
        String ds = PropKit.get("db.ds", "");
        if (StrKit.isBlank(ds)) {
            return (new String[0]);
        }
        if (ds.contains("，")) {
            new IllegalArgumentException("Cannot use ，in ds");
        }
        return ds.split(",");
    }

    /**
     * DruidPlugin
     *
     * @param ds ： property
     * @return
     */
    private DruidPlugin getDruidPlugin(String ds) {
        String url = PropKit.get(String.format("db.%s.url", ds));
        url = String.format(URL_TEMPLATE, ds, url);
        String endsWith = "?characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull";
        if (!url.endsWith(endsWith)) {
            url += endsWith;
        }
//        DruidEncryptPlugin dp = new DruidEncryptPlugin(url,
//                this.getProperty(String.format(USER_TEMPLATE, ds)),
//                this.getProperty(String.format(PASSWORD_TEMPLATE, ds)));
        DruidPlugin dp = new DruidPlugin(url,
                PropKit.get(String.format(USER_TEMPLATE, ds)),
                PropKit.get(String.format(PASSWORD_TEMPLATE, ds)));
        dp.setInitialSize(PropKit.getInt(String.format(INITSIZE_TEMPLATE, ds)));
        dp.setMaxActive(PropKit.getInt(String.format(MAXSIZE_TEMPLATE, ds)));
        dp.addFilter(new StatFilter());
        WallFilter wall = new WallFilter();
        wall.setDbType(ds);
        dp.addFilter(wall);

        if (this.geRuned) {
            dp.start();
            BaseModelGenerator baseGe = new BaseModelGenerator(this.getBaseModelPackage(), this.getBaseModelOutDir());
            ModelGeneratorExt modelGe = new ModelGeneratorExt(this.getModelPackage(), this.getBaseModelPackage(), this.getModelOutDir());
            modelGe.setGenerateDaoInModel(this.getGeDaoInModel());
            modelGe.setGenerateTableNameInModel(this.getGeTableNameInModel());
            Generator ge = new Generator(dp.getDataSource(), baseGe, modelGe);
            MappingKitGeneratorExt mappingKitGe = new MappingKitGeneratorExt(this.getModelPackage(), this.getModelOutDir());
            if (!JFinalConfigExt.DEFAULT_MAPPINGKIT_CLASS_NAME.equals(this.getMappingKitClassName())) {
                mappingKitGe.setMappingKitClassName(this.getMappingKitClassName());
            }
            mappingKitGe.setGenerateMappingArpKit(this.getGeMappingArpKit());
            mappingKitGe.setGenerateTableMapping(this.getGeTableMapping());
            ge.setMappingKitGenerator(mappingKitGe);
            ge.setGenerateDataDictionary(this.getGeDictionary());
            ge.generate();
        }

        return dp;
    }

    /**
     * 获取ActiveRecordPlugin
     *
     * @param dp DruidPlugin
     * @return
     */
    private ActiveRecordPlugin getActiveRecordPlugin(String ds, DruidPlugin dp) {
        ActiveRecordPlugin arp = new ActiveRecordPlugin(ds, dp);
        arp.setShowSql(PropKit.getBoolean("db.showsql"));

        // mapping
        if (!this.geRuned) try {
            Class<?> clazz = Class.forName(this.getModelPackage() + "." + this.getMappingKitClassName());
            Method mapping = clazz.getMethod("mapping", ActiveRecordPlugin.class);
            mapping.invoke(clazz, arp);
        } catch (NoSuchMethodException e) {
            throw (new RuntimeException(String.valueOf(e) + ",MappingKit 类中 mapping 方法没找到."));
        } catch (IllegalAccessException e) {
            throw (new RuntimeException(String.valueOf(e) + ",MappingKit 执行异常."));
        } catch (InvocationTargetException e) {
            throw (new RuntimeException(String.valueOf(e) + ",MappingKit 执行异常."));
        } catch (ClassNotFoundException e) {
            throw (new RuntimeException(String.valueOf(e) + ",MappingKit 类未找到."));
        }
        return arp;
    }

    private Boolean geDaoInModel = null;
    private Boolean geTableNameInModel = null;

    private boolean getGeDictionary() {
        return PropKit.getBoolean("ge.dict", false);
    }

    private String getBaseModelOutDir() {
        return PropKit.get("ge.base.model.outdir");
    }

    private String getBaseModelPackage() {
        return PropKit.get("ge.base.model.package");
    }

    private boolean getGeDaoInModel() {
        if (this.geDaoInModel == null) {
            this.geDaoInModel = PropKit.getBoolean("ge.model.dao", Boolean.TRUE);
        }
        return this.geDaoInModel.booleanValue();
    }

    private boolean getGeTableNameInModel() {
        if (this.geTableNameInModel == null) {
            this.geTableNameInModel = PropKit.getBoolean("ge.model.table", Boolean.TRUE);
        }
        return this.geTableNameInModel.booleanValue();
    }

    private String getModelOutDir() {
        return PropKit.get("ge.model.outdir");
    }

    private String getModelPackage() {
        return PropKit.get("ge.model.package");
    }

    private static final String DEFAULT_MAPPINGKIT_CLASS_NAME = "_MappingKit";
    private String mappingKitClassName = null;

    private String getMappingKitClassName() {
        if (this.mappingKitClassName == null) {
            this.mappingKitClassName = PropKit.get("ge.mappingkit.classname", JFinalConfigExt.DEFAULT_MAPPINGKIT_CLASS_NAME);
        }
        return this.mappingKitClassName;
    }

    private boolean getGeMappingArpKit() {
        return PropKit.getBoolean("ge.mappingarpkit", true);
    }

    private boolean getGeTableMapping() {
        return PropKit.getBoolean("ge.tablemapping", true);
    }

    //=========== Override

}
