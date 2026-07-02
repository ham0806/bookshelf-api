package dev.hambacon.bookshelf

import dev.hambacon.bookshelf.api.AuthorResponse
import dev.hambacon.bookshelf.api.BookResponse
import dev.hambacon.bookshelf.api.CreateAuthorRequest
import dev.hambacon.bookshelf.api.CreateBookRequest
import dev.hambacon.bookshelf.api.UpdateAuthorRequest
import dev.hambacon.bookshelf.api.UpdateBookRequest
import dev.hambacon.bookshelf.domain.PublicationStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    statements = [
        "DELETE FROM book_authors",
        "DELETE FROM books",
        "DELETE FROM authors",
    ],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
class BookshelfApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
) : PostgresIntegrationTest() {
    @Test
    fun `著者に紐づく書籍を取得できる`() {
        val author = createAuthor("夏目 漱石", LocalDate.of(1867, 2, 9))
        val firstBook = createBook(
            title = "吾輩は猫である",
            price = BigDecimal("1100.00"),
            authorIds = listOf(author.id),
            publicationStatus = PublicationStatus.PUBLISHED,
        )
        val secondBook = createBook(
            title = "こころ",
            price = BigDecimal("990.00"),
            authorIds = listOf(author.id),
            publicationStatus = PublicationStatus.PUBLISHED,
        )

        val response = restTemplate.exchange(
            "/api/authors/${author.id}/books",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<BookResponse>>() {},
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(setOf(firstBook.id, secondBook.id), response.body?.map { it.id }?.toSet())
        assertTrue(response.body.orEmpty().all { it.publicationStatus == PublicationStatus.PUBLISHED })
        assertTrue(response.body.orEmpty().all { book -> book.authors.map { it.id } == listOf(author.id) })
    }

    @Test
    fun `書籍に複数著者を紐づけられる`() {
        val firstAuthor = createAuthor("共著者 A", LocalDate.of(1980, 1, 1))
        val secondAuthor = createAuthor("共著者 B", LocalDate.of(1981, 1, 1))

        val book = createBook(
            title = "共著の本",
            price = BigDecimal("1200.00"),
            authorIds = listOf(firstAuthor.id, secondAuthor.id),
            publicationStatus = PublicationStatus.UNPUBLISHED,
        )

        val response = restTemplate.getForEntity(
            "/api/books/${book.id}",
            BookResponse::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(setOf(firstAuthor.id, secondAuthor.id), response.body?.authors?.map { it.id }?.toSet())
    }

    @Test
    fun `著者を更新できる`() {
        val author = createAuthor("芥川 龍之介", LocalDate.of(1892, 3, 1))

        val response = restTemplate.exchange(
            "/api/authors/${author.id}",
            HttpMethod.PUT,
            HttpEntity(
                UpdateAuthorRequest(
                    name = "芥川 龍之介 更新",
                    birthDate = LocalDate.of(1892, 3, 2),
                ),
            ),
            AuthorResponse::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("芥川 龍之介 更新", response.body?.name)
        assertEquals(LocalDate.of(1892, 3, 2), response.body?.birthDate)
    }

    @Test
    fun `書籍を更新できる`() {
        val firstAuthor = createAuthor("樋口 一葉", LocalDate.of(1872, 5, 2))
        val secondAuthor = createAuthor("与謝野 晶子", LocalDate.of(1878, 12, 7))
        val book = createBook(
            title = "初期タイトル",
            price = BigDecimal("500.00"),
            authorIds = listOf(firstAuthor.id),
            publicationStatus = PublicationStatus.UNPUBLISHED,
        )

        val response = restTemplate.exchange(
            "/api/books/${book.id}",
            HttpMethod.PUT,
            HttpEntity(
                UpdateBookRequest(
                    title = "更新後タイトル",
                    price = BigDecimal("750.00"),
                    authorIds = listOf(secondAuthor.id),
                    publicationStatus = PublicationStatus.PUBLISHED,
                ),
            ),
            BookResponse::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("更新後タイトル", response.body?.title)
        assertEquals(BigDecimal("750.00"), response.body?.price)
        assertEquals(PublicationStatus.PUBLISHED, response.body?.publicationStatus)
        assertEquals(listOf(secondAuthor.id), response.body?.authors?.map { it.id })
    }

    @Test
    fun `書籍価格は0以上でなければならない`() {
        val author = createAuthor("宮沢 賢治", LocalDate.of(1896, 8, 27))

        val response = restTemplate.postForEntity(
            "/api/books",
            CreateBookRequest(
                title = "銀河鉄道の夜",
                price = BigDecimal("-1.00"),
                authorIds = listOf(author.id),
            ),
            String::class.java,
        )

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `書籍には1人以上の著者が必要`() {
        val response = restTemplate.postForEntity(
            "/api/books",
            CreateBookRequest(
                title = "著者なしの本",
                price = BigDecimal("1000.00"),
                authorIds = emptyList(),
            ),
            String::class.java,
        )

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `存在しない著者を指定した書籍登録は拒否する`() {
        val response = restTemplate.postForEntity(
            "/api/books",
            CreateBookRequest(
                title = "存在しない著者の本",
                price = BigDecimal("1000.00"),
                authorIds = listOf(UUID.randomUUID()),
            ),
            String::class.java,
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertTrue(response.body?.contains("著者が見つかりません") == true)
    }

    @Test
    fun `著者の生年月日は現在日以前でなければならない`() {
        val response = restTemplate.postForEntity(
            "/api/authors",
            CreateAuthorRequest(
                name = "未来の著者",
                birthDate = LocalDate.now().plusDays(1),
            ),
            String::class.java,
        )

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `出版済み書籍を未出版へ戻せない`() {
        val author = createAuthor("太宰 治", LocalDate.of(1909, 6, 19))
        val book = createBook(
            title = "人間失格",
            price = BigDecimal("900.00"),
            authorIds = listOf(author.id),
            publicationStatus = PublicationStatus.PUBLISHED,
        )

        val response = restTemplate.exchange(
            "/api/books/${book.id}",
            HttpMethod.PUT,
            HttpEntity(
                UpdateBookRequest(
                    title = "人間失格",
                    price = BigDecimal("900.00"),
                    authorIds = listOf(author.id),
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                ),
            ),
            String::class.java,
        )

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body?.contains("出版済みの書籍は未出版へ変更できません") == true)
    }

    @Test
    fun `OpenAPI 仕様を取得できる`() {
        val response = restTemplate.getForEntity(
            "/v3/api-docs",
            String::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body?.contains("\"openapi\"") == true)
        assertTrue(response.body?.contains("\"/api/books\"") == true)
        assertTrue(response.body?.contains("\"/api/authors\"") == true)
    }

    @Test
    fun `Swagger UI を表示できる`() {
        val response = restTemplate.getForEntity(
            "/swagger-ui/index.html",
            String::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body?.contains("Swagger UI") == true)
    }

    private fun createAuthor(name: String, birthDate: LocalDate): AuthorResponse {
        val response = restTemplate.postForEntity(
            "/api/authors",
            CreateAuthorRequest(name = name, birthDate = birthDate),
            AuthorResponse::class.java,
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        return requireNotNull(response.body)
    }

    private fun createBook(
        title: String,
        price: BigDecimal,
        authorIds: List<UUID>,
        publicationStatus: PublicationStatus,
    ): BookResponse {
        val response = restTemplate.postForEntity(
            "/api/books",
            CreateBookRequest(
                title = title,
                price = price,
                authorIds = authorIds,
                publicationStatus = publicationStatus,
            ),
            BookResponse::class.java,
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        return requireNotNull(response.body)
    }
}
