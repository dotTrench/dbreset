create table circle (id int primary key, parentid int NULL);
alter table circle add constraint FK_Parent foreign key (parentid) references circle (id);
