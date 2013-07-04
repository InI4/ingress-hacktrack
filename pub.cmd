set pf=_all_docs.js out.html
call fsn
call git add %pf% out.html
call git commit %pf% -m "New data to publish."
call git push
call git status
