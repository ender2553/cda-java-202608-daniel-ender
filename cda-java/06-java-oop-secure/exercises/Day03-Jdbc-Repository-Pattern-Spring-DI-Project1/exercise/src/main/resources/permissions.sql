--=====================================
--NOTE: RUN THIS SCRIPT LAST
--=====================================

create role secops_app with login password 'Change-Me';
grant all privileges on database secops_exercise to secops_app;
grant all privileges on database secops_exercise_test to secops_app;


grant usage on schema public to secops_app;

grant select, insert, update, delete
      on all tables in schema public
          to secops_app;

grant usage, select
             on all sequences in schema public
                 to secops_app;