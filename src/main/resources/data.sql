-- DEV PROFILE ONLY — runs when spring.datasource.initialization-mode=always (dev only).
-- Passwords are BCrypt-encoded (cost=11). Dev credentials:
--   admin    / Admin@2026
--   hr_user  / HrOfficer@2026
--   emp_user / Employee@2026

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
