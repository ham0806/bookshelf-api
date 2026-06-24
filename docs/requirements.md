# 要件整理

## 技術要件

- 言語は Kotlin
- フレームワークは Spring Boot
- RDB アクセスには jOOQ を使う
- フロントエンドは不要
- 可能な範囲で単体テストを作成する

## 必要な機能

- 書籍情報を RDB に登録できる
- 書籍情報を更新できる
- 著者情報を RDB に登録できる
- 著者情報を更新できる
- 著者に紐づく書籍を取得できる

## 書籍の属性と制約

- タイトルを持つ
- 価格を持つ
- 価格は 0 以上
- 著者を最低 1 人持つ
- 著者は複数指定できる
- 出版状況は未出版または出版済み
- 出版済みの書籍は未出版へ変更できない

## 著者の属性と制約

- 名前を持つ
- 生年月日を持つ
- 生年月日は現在日以前
- 著者は複数の書籍を執筆できる

## この実装での API 対応

- `POST /api/authors`: 著者登録
- `PUT /api/authors/{authorId}`: 著者更新
- `GET /api/authors/{authorId}`: 著者取得
- `POST /api/books`: 書籍登録
- `PUT /api/books/{bookId}`: 書籍更新
- `GET /api/books/{bookId}`: 書籍取得
- `GET /api/authors/{authorId}/books`: 著者に紐づく書籍取得
