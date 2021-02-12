FROM mysql:5.7

# 第一次启动时会自动执行这个目录下的.sh, .sql, .sql.gz文件
ENV AUTO_RUN_DIR /docker-entrypoint-initdb.d
ENV WORK_PATH /usr/local/work

RUN mkdir -p $WORK_PATH
COPY auto_run_sql $WORK_PATH/
COPY init.sh $AUTO_RUN_DIR/
RUN chmod a+x $AUTO_RUN_DIR/init.sh