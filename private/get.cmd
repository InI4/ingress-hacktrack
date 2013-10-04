@set fi=_all_docs.js
@rem ntlmaps
rem @set http_proxy=http://localhost:8000
rem @set https_proxy=http://localhost:8000
@rem fiddler
@set http_proxy=http://localhost:8888
rem wget -N --verbose --output-document=%fi% --http-user=InI4 --http-passwd=Basino48 --proxy=on http://hugo.brokenpipe.de:8080/couchdb/hack-tracker/_all_docs?include_docs=true 
wget -N --verbose --output-document=%fi% --http-user=InI4 --http-passwd=Basino48  http://hugo.brokenpipe.de:8080/couchdb/hack-tracker/_all_docs?include_docs=true
@call git diff --minimal -U0 %fi% | head -7
@call git status
