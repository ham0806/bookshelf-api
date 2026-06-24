CREATE TABLE authors (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    birth_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_authors_name_not_blank CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT chk_authors_birth_date_not_future CHECK (birth_date <= CURRENT_DATE)
);

CREATE TABLE books (
    id UUID PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    publication_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_books_title_not_blank CHECK (LENGTH(TRIM(title)) > 0),
    CONSTRAINT chk_books_price_not_negative CHECK (price >= 0),
    CONSTRAINT chk_books_publication_status CHECK (publication_status IN ('UNPUBLISHED', 'PUBLISHED'))
);

CREATE TABLE book_authors (
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES authors(id) ON DELETE RESTRICT,
    PRIMARY KEY (book_id, author_id)
);

CREATE INDEX idx_book_authors_author_id ON book_authors(author_id);
