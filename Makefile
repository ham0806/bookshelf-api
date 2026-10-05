.PHONY: test check

# 統合テストは PostgreSQL が必要なため、先に Docker Compose で起動する。
test:
	docker compose up -d --wait postgres
	./gradlew test --no-daemon

# DB 不要のコンパイル確認（main + test ソース）。
check:
	./gradlew testClasses --no-daemon
