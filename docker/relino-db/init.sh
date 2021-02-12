#!/bin/bash
mysql -uroot -p$MYSQL_ROOT_PASSWORD << EOF
source $WORK_PATH/init_db.sql;