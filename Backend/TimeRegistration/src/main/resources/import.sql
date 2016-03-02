INSERT INTO employee (id, email, firstName, hash, lastName, salt, username, version) VALUES (1, 'bla@bla.com', 'frederik', 'X9CsWfFFr5oh1qVMux/gkhmSpuRkWNC/Kbfhz3FG8mA=', 'de smedt', '1och84d0nmj8t6qrl12a9k5cm4dhh04lnpb1l30lgefq1h2tgal4', 'fds', 0);
INSERT INTO employee (id, email, firstName, hash, lastName, salt, username, version) VALUES (2, 'bla2@bla.com', 'brent', 'r2Mf9eycuUxWrNr7FFu90ZOZhZO+tT/HDYRDxlydJH0=', 'couck', '1jdbnoiqap5fen2hbb1t6tfpbbtuo9qqjvhj7ela9gdrh1tvkjce', 'bc', 0);
INSERT INTO project (id, description, name, endDate, projectNr, startDate, version) VALUES (3, 'Project 1 description', 'Project 1', '2016-03-01', 9, '2016-03-02', 0);
INSERT INTO project (id, description, name, endDate, projectNr, startDate, version) VALUES (4, 'Project 2 description', 'Project 1', '2016-03-01', 3, '2016-03-02', 0);
INSERT INTO project (id, description, name, endDate, projectNr, startDate, version) VALUES (5, 'Project 3 description', 'Project 1', '2016-03-01', 5, '2016-03-02', 0);
INSERT INTO employee_project (employee_id, project_id) VALUES (2, 3);
TRUNCATE TABLE hibernate_sequence;
INSERT INTO hibernate_sequence (next_val) VALUES (6), (6), (6);