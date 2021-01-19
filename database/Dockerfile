FROM bitnami/postgresql:11.9.0
COPY ./lobby/src/main/resources/conf/sql/*.structure-*.sql /docker-entrypoint-initdb.d/
ENV POSTGRESQL_DATABASE=lobby
EXPOSE 5432
