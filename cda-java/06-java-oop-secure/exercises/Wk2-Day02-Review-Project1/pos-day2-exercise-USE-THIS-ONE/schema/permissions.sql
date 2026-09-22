create role quickpay_app_user with login password 'changeme';

-- quickpay_app_user
grant all privileges on database quickpay_pos to quickpay_app_user;



grant usage on schema public to quickpay_app_user;

grant select, insert, update, delete
      on all tables in schema public
          to quickpay_app_user;

grant usage, select
             on all sequences in schema public
                 to quickpay_app_user;


GRANT USAGE, CREATE ON SCHEMA public TO quickpay_app_user;

--ALTER TABLE merchant OWNER TO quickpay_app_user;
--ALTER TABLE transaction OWNER TO quickpay_app_user;