create table user
(
ID int auto_increment primary key not null ,
ACCOUNT_ID varchar(100),
NAME varchar(50),
TOKEN varchar(36),
GMT_CREATE bigint,
GMT_MODIFIED bigint

);