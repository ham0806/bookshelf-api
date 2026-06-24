# Repository Instructions

## Language

- この repository での説明、進捗報告、レビューコメントは原則として日本語で行う。
- コード識別子、コマンド、ログ、エラーメッセージ、外部仕様の原文はそのまま扱う。
- 新規コメントは、既存コードの規約に反しない範囲で日本語にする。

## Project Overview

この repository は、書籍と著者を管理するバックエンド API です。

- Kotlin
- Spring Boot
- jOOQ
- Flyway
- PostgreSQL
- H2 for tests

フロントエンドはありません。API は JSON を受け取り、JSON を返します。

## Commands

Docker を使える環境では、以下を優先する。

```powershell
docker compose run --rm test
```

アプリケーションを起動する場合:

```powershell
docker compose up app
```

Docker Compose のリソースを片付ける場合:

```powershell
docker compose down
```

ローカルに Java 21 と Gradle がある場合のみ、以下も使用できる。

```powershell
gradle test
gradle bootRun
```

## Architecture

- `api`: Controller、request/response DTO、API 例外変換を置く。
- `application`: 業務ルールと transaction 境界を置く。
- `domain`: ドメインモデルと enum を置く。
- `infrastructure`: jOOQ による RDB アクセスを置く。
- `src/main/resources/db/migration`: Flyway migration を置く。

Controller に業務ルールを寄せない。DB アクセスを Service に直接書かず、Repository interface 経由にする。

## Domain Rules

- 書籍価格は 0 以上。
- 書籍は 1 人以上の著者を持つ。
- 書籍は複数の著者を持てる。
- 著者は複数の書籍に紐づけられる。
- 著者の生年月日は現在日以前。
- 出版済みの書籍は未出版へ戻せない。

これらのルールを変更する場合は、API 統合テストまたは Service 単体テストを必ず更新する。

## Implementation Guidelines

- 既存構成に沿った小さな変更を優先する。
- Kotlin では `val` を優先し、不要な再代入を避ける。
- nullable 型は意図を明確にし、存在しない DB row は制御された例外に変換する。
- 入力制約は Bean Validation と DB 制約の両方を意識する。
- schema 変更は Flyway migration で行う。
- jOOQ codegen は使っていない。現状は小規模実装として `DSLContext` と明示的なテーブル定義を使う。
- 新しい依存関係、広範囲リファクタ、API 互換性に影響する変更は、実装前に理由を明確にする。

## Tests

- Service の業務ルールは `BookServiceTest` のような単体テストで確認する。
- API と DB 統合の動作は `BookshelfApiTest` のような Spring Boot 統合テストで確認する。
- 変更後は、可能な限り `docker compose run --rm test` を実行する。
- テスト実行後に Docker Compose のリソースが残った場合は `docker compose down` で片付ける。

## Repository Hygiene

- `build/`、`.gradle/`、IDE 設定、ローカル作業ディレクトリ、生成ログは commit しない。
- 秘密情報、個人情報、外部サービスの token、実データを repository に追加しない。
- 提出用の README と docs には、外部選考元や元課題 URL など、この repository の汎用性を損なう情報を入れない。
- 明示的に依頼されない限り、`git commit`、push、release、deploy は行わない。
