//package com.lazy.jmcomic.api.v1.annotation;
//
//import com.lazy.jmcomic.api.v1.constant.Properties;
//import org.springframework.beans.factory.annotation.Value;
//
//import java.lang.annotation.*;
//
///**
// * <p>组合注解：封装各 jmcomic @ConfigurationProperties 的 @Value 注入路径</p>
// * <p>注意：只用于注入顶级 @ConfigurationProperties bean，嵌套对象应通过构造器注入获取</p>
// * <p>使用示例：</p>
// * <pre>
// *   {@literal @}JmValue.Image
// *   private ImageProperties imageProperties;
// *
// *   // 在构造器中获取嵌套对象
// *   public ImageHttpPingTask(ImageProperties imageProperties) {
// *       this.httping = imageProperties.httping();
// *   }
// * </pre>
// */
//public @interface JmValue {
//
//    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
//    @Retention(RetentionPolicy.RUNTIME)
//    @Value("${" + Properties.LOGIN + "}")
//    @interface Login {}
//
//    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
//    @Retention(RetentionPolicy.RUNTIME)
//    @Value("${" + Properties.IMAGE + "}")
//    @interface Image {}
//
//    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
//    @Retention(RetentionPolicy.RUNTIME)
//    @Value("${" + Properties.WEBSITE + "}")
//    @interface Website {}
//
//    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
//    @Retention(RetentionPolicy.RUNTIME)
//    @Value("${" + Properties.CONNECTION + "}")
//    @interface Proxy {}
//}