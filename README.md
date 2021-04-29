# newscollector

### URLs: 
*/info (Metrics)*  
*/truncateTables (Delete all Entries in content, rss_feed and publisher table)*  
*/uploadCSV  (Upload RssFeeds)*  
*/receiveCSV* 

### Helm Chart
create a namespace
```
$ kubectl create namespace newscollector
```
Update Dependcies
```
$ helm dependency update newscollector-chart
```
Dry run, and check out the yamls
```
$ helm upgrade --install newscollector-chart --namespace newscollector newscollector-chart --dry-run
```
Run without dry-run
```
$ helm upgrade --install newscollector-chart --namespace newscollector newscollector-chart
```
for local shell
```
$ k exec -it newscollector-chart-mysql-0 /bin/sh
```
Cd to the folder with the sql schemas (Need to include a use for the database and delete index table)
```
$ kubectl -n newscollector exec -i newscollector-chart-mysql-0 -- mysql -u root -psecret < *.sql
```
The last step is to update a file for the news Upload File for News
```
$ k port-forward newscollector-chart-8647ccfc67-v7wz5 8080
```
Login (user/user) and access /uploadCSV to upload the feed csv
