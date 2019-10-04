create table parent (id int primary key, childid int NULL);
create table child (id int primary key, parentid int NULL);
alter table parent add constraint FK_Child foreign key (ChildId) references child (Id);
alter table child add constraint FK_Parent foreign key (ParentId) references parent (Id);
