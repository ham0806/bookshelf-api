# bookshelf-api

書籍と著者を管理するバックエンド API です。

## 構成

- Kotlin
- Spring Boot
- jOOQ
- Flyway
- PostgreSQL
- H2 for test

フロントエンドはありません。API は JSON を受け取り、JSON を返します。

## 起動

Docker が使える場合は、PostgreSQL とアプリケーションをまとめて起動できます。ローカルに Java や Gradle をインストールしていなくても起動できます。

```powershell
docker compose up app
```

ローカルに Java 21 と Gradle がある場合は、PostgreSQL を起動したうえで以下でも起動できます。

```powershell
gradle bootRun
```

デフォルトの接続先は以下です。

- URL: `jdbc:postgresql://localhost:5432/bookshelf`
- User: `bookshelf`
- Password: `bookshelf`

環境変数 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD` で上書きできます。

## テスト

Docker の Gradle イメージを使う場合は以下です。

```powershell
docker compose run --rm test
```

ローカルに Java 21 と Gradle がある場合は以下でも実行できます。

```powershell
gradle test
```

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
- DB 操作は jOOQ の `DSLContext` で実装しています。小規模な課題実装のため、jOOQ codegen ではなく明示的なテーブル定義を使っています。
- Controller は API の受け口に寄せ、業務ルールは Service に集約しています。
- 入力値の基本制約は Bean Validation と DB 制約の両方で守ります。
