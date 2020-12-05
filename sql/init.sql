create database relino;
use relino;

drop table if exists job;
CREATE TABLE job
(
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    job_id              varchar(64)     not null comment 'job_id',
    idempotent_id       varchar(64)     not null comment '幂等字段',
    job_code            varchar(32)     not null default 'none' comment 'job 类型',

    is_delay_job        tinyint         not null comment '是否为延迟job',
    begin_time          datetime        not null comment '如果是延迟job, 存储job开始执行时间，否则为当前时间',

    /* 公共参数 */
    common_attr         varchar(12000)  not null comment '',

    /* job 将要执行的时间 */
    will_execute_time datetime        not null comment '',
    job_status          tinyint not null comment 'job 状态: delay 1, runnable 2, finished 3',
    /* 执行顺序. 延迟 -1, 立即执行 1, 延迟变为可执行后 自增的数 */
    execute_order       bigint not null default -1 comment '',

    /* main oper */
    m_action_id         varchar(64)     not null comment '',
    m_oper_status       tinyint         not null comment '',
    m_execute_count     tinyint         not null comment '',
    m_retry_policy_id   varchar(64)     not null comment '',
    m_max_retry         tinyint         not null comment '',

    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (id),
    UNIQUE uniq_job_id (job_id),
    UNIQUE uniq_idempotent_id (idempotent_id),
    index  idx_execute_order(execute_order),

    index  idx_will_execute_time_job_status(will_execute_time, job_status)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4;

drop table if exists kv;
CREATE TABLE kv
(
    k varchar(128) NOT NULL COMMENT 'id',
    v varchar(128)  not null comment 'value',

    primary key (k)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4;

insert into kv(k, v) value ('execute_queue_cursor', '-1');
insert into kv(k, v) value ('execute_order', '1');
insert into kv(k, v) value ('dead_job_watch_dog', '-1');

drop table if exists execute_time;
CREATE TABLE execute_time
(
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    execute_order       bigint not null  comment '执行job的execute_order',
    execute_job_time    datetime        not null comment '执行job的时间',

    primary key (id),
    key idx_id_execute_job_time(id, execute_job_time)

) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4;

