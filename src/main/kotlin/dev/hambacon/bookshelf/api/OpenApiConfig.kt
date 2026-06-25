package dev.hambacon.bookshelf.api

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun bookshelfOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("bookshelf-api")
                    .version("0.0.1")
                    .description("書籍と著者を管理するバックエンド API です。"),
            )
}
