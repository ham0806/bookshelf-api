# bookshelf-api

書籍と著者を管理するバックエンド API です。

## 使用技術

この repository はフロントエンドを持たないバックエンド API です。HTTP リクエストとして JSON を受け取り、処理結果を JSON で返します。

### アプリケーション

| 技術 | 用途 |
| --- | --- |
| Kotlin 1.9.25 | アプリケーション本体とテストコードの実装言語として使用しています。 |
| Java 21 | Kotlin/JVM の実行基盤として使用しています。Gradle toolchain で Java 21 を指定しています。 |
| Spring Boot 3.3.13 | Web API、DI、設定管理、transaction 管理、テスト起動の基盤として使用しています。3.3 系の最新パッチを使い、互換性を保ちながら修正済みの不具合と脆弱性対応を取り込みます。 |
| Spring Web | `@RestController` による JSON API のエンドポイント実装に使用しています。 |
| Spring Validation | request DTO の入力制約を Bean Validation で検証するために使用しています。 |
| Springdoc OpenAPI / Swagger UI | Controller と DTO から OpenAPI 仕様を自動生成し、Swagger UI で API を確認するために使用しています。 |
| Jackson Kotlin module | Kotlin の data class と JSON の相互変換に使用しています。 |

### データベース

| 技術 | 用途 |
| --- | --- |
| PostgreSQL | アプリケーション実行時の RDB として使用しています。Docker Compose では `postgres:16-alpine` を起動します。 |
| jOOQ | Repository 層で SQL を組み立て、RDB にアクセスするために使用しています。Flyway migration から jOOQ codegen を実行し、生成された table / field 定義を Repository で参照します。 |
| Flyway | `src/main/resources/db/migration` 配下の migration による DB schema 管理に使用しています。 |
| Testcontainers / PostgreSQL | 統合テスト用に PostgreSQL container を起動します。本番実行時と同じ DB 方言で Flyway migration、jOOQ、制約の動作を確認します。 |

### ビルド・実行・テスト

| 技術 | 用途 |
| --- | --- |
| Gradle | build、test、bootRun、jOOQ codegen の実行に使用しています。Docker Compose では `gradle:8.10.2-jdk21` image を使用します。 |
| Gradle Wrapper | ローカルに Gradle をインストールしていない環境でも、同じ Gradle 8.10.2 で build、test、bootRun を実行するために使用しています。 |
| mise | ローカル開発で Java 21 を揃えるための任意ツールとして使用できます。Docker Compose を使う場合は不要です。 |
| Docker Compose | PostgreSQL とアプリケーション、またはテスト実行用 Gradle 環境をまとめて起動するために使用しています。 |
| JUnit 5 / Spring Boot Test | Service の業務ルール単体テストと、HTTP API から PostgreSQL まで含めた統合テストに使用しています。 |

## 起動

Docker が使える場合は、PostgreSQL とアプリケーションをまとめて起動できます。ローカルに Java や Gradle をインストールしていなくても起動できます。

```powershell
docker compose up app
```

ローカルに Java 21 がある場合は、PostgreSQL を起動したうえで `local` profile を指定して Gradle Wrapper でも起動できます。

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

mise を使う場合は、Java 21 をこの repository の設定に合わせて用意できます。

```powershell
mise install
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

`local` profile のデフォルト接続先は以下です。

- URL: `jdbc:postgresql://localhost:5432/bookshelf`
- User: `bookshelf`
- Password: `bookshelf`

環境変数 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD` で上書きできます。profile 未指定時は環境変数の指定を必須にし、公開環境でローカル用の接続情報を誤用しにくい構成にしています。

## テスト

Docker の Gradle イメージを使う場合は以下です。

```powershell
docker compose run --rm test
```

このテストは Testcontainers で PostgreSQL を起動するため、Docker daemon に接続できる環境が必要です。

ローカルに Java 21 がある場合は以下でも実行できます。

```powershell
.\gradlew.bat test
```

mise を使う場合は以下です。

```powershell
mise install
.\gradlew.bat test
```

## CI / 品質確認

GitHub Actions でテストとセキュリティ確認を実行します。

- `CI`: push / pull request 時に `docker compose run --rm test` を実行し、Service 単体テストと Spring Boot 統合テストを確認します。実行後は Docker Compose のリソースを片付けます。
- `CodeQL`: Kotlin / Java 向けの静的解析を実行します。解析前に `gradle test --no-daemon` で build 可能な状態を確認します。
- `Secret Scan`: Gitleaks により、token や秘密情報を誤って commit していないか確認します。
- `Dependabot`: Gradle、Docker Compose、GitHub Actions の依存関係更新を週次で確認します。

README や docs だけの変更では、通常の CI テストは `paths-ignore` により省略されます。

## API 仕様

OpenAPI 仕様は Springdoc OpenAPI により、実行中のアプリケーションから自動生成します。

アプリケーション起動後、以下の URL で確認できます。

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

## API 例

著者を登録します。

```powershell
curl.exe -X POST http://localhost:8080/api/authors `
  -H "Content-Type: application/json" `
  -d '{ "name": "夏目 漱石", "birthDate": "1867-02-09" }'
```

書籍を登録します。`authorIds` には登録済み著者の ID を指定します。

```powershell
curl.exe -X POST http://localhost:8080/api/books `
  -H "Content-Type: application/json" `
  -d '{ "title": "吾輩は猫である", "price": 1100, "authorIds": ["<author-id>"], "publicationStatus": "PUBLISHED" }'
```

著者に紐づく書籍を取得します。

```powershell
curl.exe http://localhost:8080/api/authors/<author-id>/books
```

## 主要な制約

- 書籍価格は 0 以上
- 書籍は 1 人以上の著者を持つ
- 書籍は複数著者を持てる
- 著者は複数書籍に紐づけられる
- 著者の生年月日は現在日以前
- 出版済みの書籍は未出版へ戻せない

## 実装メモ

- DB スキーマは Flyway migration で管理します。
- DB 操作は jOOQ の `DSLContext` で実装しています。table / field 定義は Flyway migration から jOOQ codegen で生成し、schema 変更時に Kotlin 側の参照漏れを検知しやすくしています。
- Controller は API の受け口に寄せ、業務ルールは Service に集約しています。
- 入力値の基本制約は Bean Validation と DB 制約の両方で守ります。
- ID は外部 API に露出する識別子として UUID を採用しています。小規模な書籍管理 API では連番 ID でも成立しますが、URL から件数や登録順を推測されにくく、将来のデータ統合時にも衝突しにくい点を優先しています。詳細は `docs/adr/0001-id-database-and-test-strategy.md` を参照してください。
