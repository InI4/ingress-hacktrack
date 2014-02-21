setlocal
pushd private
rem call get.cmd
call repl.cmd
popd
set pf=out.html out_bonus.html
call fsn.cmd
call git add %pf% 
call git commit -m "New data to publish."
call git push
call git status
endlocal
