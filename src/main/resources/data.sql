-- DEV PROFILE ONLY — runs when spring.datasource.initialization-mode=always (dev only).
-- Passwords are BCrypt-encoded (cost=11). Dev credentials:
--   admin       / Admin@2026
--   hr_user     / HrOfficer@2026
--   emp_user    / Employee@2026
--   supervisor  / Supervisor@2026   (CR 016 v2)
--   council_sec / Council@2026      (CR 016 v2)
--   vice_mayor  / ViceMayor@2026    (CR 016 v2)

insert into employee(id,username,password,emp_hash_code,first_name,last_name,gender,mobile_no1,email1,status,user_type)
values(1,'admin','$2a$11$Pd25BmgpmecsbfDxIBVcpeZf05/Xtvs1OoJb2jqc6a2QqRyzSi2Um','isHklfn35Rgnd456556rfgngdfg12','Ian','Orozco','M','09062794574','admin@gmail.com','A','ROLE_ADMIN')
on duplicate key update
emp_hash_code=values(emp_hash_code),
first_name=values(first_name),
last_name=values(last_name),
gender=values(gender),
mobile_no1=values(mobile_no1),
email1=values(email1),
status=values(status),
user_type=values(user_type);

insert into employee(id,username,password,emp_hash_code,first_name,last_name,gender,mobile_no1,email1,status,user_type)
values(2,'hr_user','$2a$11$fF7wrguVLnXiMCUp9ceL0.l1vuVUBXBoeCwUjPX7VtOCaPSXfa05O','hrHklfn35Rgnd456556rfgngdfg12','HR','Officer','M','09062794575','hr@gmail.com','A','ROLE_HR')
on duplicate key update
emp_hash_code=values(emp_hash_code),
first_name=values(first_name),
last_name=values(last_name),
gender=values(gender),
mobile_no1=values(mobile_no1),
email1=values(email1),
status=values(status),
user_type=values(user_type);

insert into employee(id,username,password,emp_hash_code,first_name,last_name,gender,mobile_no1,email1,status,user_type)
values(3,'emp_user','$2a$11$HehLSgAFZuXpiHtXfrQ.aOaZ9amJD9OojlcVVnmXWRlVp8ZEjrmHe','empHklfn35Rgnd456556rfgngdfg12','John','Employee','M','09062794576','employee@gmail.com','A','ROLE_EMPLOYEE')
on duplicate key update
emp_hash_code=values(emp_hash_code),
first_name=values(first_name),
last_name=values(last_name),
gender=values(gender),
mobile_no1=values(mobile_no1),
email1=values(email1),
status=values(status),
user_type=values(user_type);

-- CR 016 v2: leave workflow actor accounts. No fixed ids (auto-increment) and
-- keyed on the unique username so re-runs update rather than duplicate. Status
-- 'N/A' keeps these service accounts out of the year-end mandatory deduction.
insert into employee(username,password,emp_hash_code,first_name,last_name,status,user_type)
values('supervisor','$2a$11$33TsPVTYL3lvJon46zvC/OYS8Ze3GojkLjGrlvsyVcOhOXylstlda','supHklfn35Rgnd456556rfgngdfg12','Department','Supervisor','N/A','ROLE_SUPERVISOR')
on duplicate key update
status=values(status),
user_type=values(user_type);

insert into employee(username,password,emp_hash_code,first_name,last_name,status,user_type)
values('council_sec','$2a$11$wf1d32eiyhptwEIFVwHR8.PPie1SQg2PAUzMTHfgUsDtRk6C3LH2G','cscHklfn35Rgnd456556rfgngdfg12','Hans Roger','Luna','N/A','ROLE_COUNCIL')
on duplicate key update
status=values(status),
user_type=values(user_type);

insert into employee(username,password,emp_hash_code,first_name,last_name,status,user_type)
values('vice_mayor','$2a$11$lv72MUGmkzCLtn6avB/Iqe6Q5E5RZt34jo4AlaVdyeFJVia3tdtrW','vmHklfn35Rgnd456556rfgngdfg123','Angela Lei','Atienza','N/A','ROLE_VICEMAYOR')
on duplicate key update
status=values(status),
user_type=values(user_type);
