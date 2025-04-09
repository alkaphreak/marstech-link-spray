To find branches ordered by commit date:

``` sh
git for-each-ref \
    --sort=committerdate refs/heads/ \
    --format="%(HEAD) %(align:50)%(refname:short)%(end) - %(committerdate:iso)"
```
