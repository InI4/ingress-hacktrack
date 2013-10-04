set pf=out.html
call fsn
call git add %pf% 
call git commit %pf% -m "New data to publish."
call git push
call git status
