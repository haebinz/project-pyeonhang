package projecct.pyeonhang.common.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    //@Value("${server.file.gallery.path}")
   private String filePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/imgs/**")
                .addResourceLocations("file:" + filePath )
                .setCachePeriod(0)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    //파일제한
    @Bean
    public MultipartConfigElement multipartConfigElement(){
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.of(50, DataUnit.MEGABYTES));
        factory.setMaxRequestSize(DataSize.of(50, DataUnit.MEGABYTES));
        return factory.createMultipartConfig();
    }
}
