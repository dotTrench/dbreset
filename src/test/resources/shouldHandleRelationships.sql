create table foo (value int, primary key (value));
create table baz (value int, foovalue int, constraint FK_Foo foreign key (foovalue) references foo (value));
