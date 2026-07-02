# 0001. ID、DB アクセス、テスト DB の方針

## Status

Accepted

## Context

この API は書籍と著者を管理し、ID を HTTP API の path や response に含める。永続化は PostgreSQL、DB アクセスは jOOQ、schema 管理は Flyway で行う。

初期実装では小規模さを優先して jOOQ の table / field 定義を手書きし、統合テストには H2 の PostgreSQL mode を使っていた。しかし、schema とコードのずれ、PostgreSQL 固有の挙動差、jOOQ の強みを活かしきれていない点が残る。

## Decision

- API に露出する `authors.id` と `books.id` は UUID とする。
- 連番 ID は採用しない。小規模 API では連番でも成立するが、登録順や件数を URL から推測されにくく、将来のデータ移行や外部連携時に衝突しにくい UUID を優先する。
- jOOQ の table / field 定義は手書きせず、Flyway migration から codegen する。
- 統合テストは H2 ではなく Testcontainers の PostgreSQL で実行する。
- デフォルト profile は公開環境での誤用を避けるため、DB 接続情報と Swagger UI を明示設定にする。`local` と `test` profile だけローカル用のデフォルトや API docs を有効にする。

## Consequences

- UUID は index サイズと locality の面で連番 ID より不利になる可能性がある。ただし、この API の想定規模では可読性、外部公開時の推測しにくさ、衝突回避を優先する。
- jOOQ codegen により、migration と Repository 実装の対応が build 時に見えやすくなる。
- Testcontainers によりテスト実行には Docker daemon が必要になるが、本番 DB と異なる H2 方言での見落としを減らせる。
