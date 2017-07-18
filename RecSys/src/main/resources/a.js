module.exports = {
    "_id": "_design/res",
   "_rev": "7-5ad9f6505a19f341e7fb48b5df5f6093",
    "language": "javascript",
    "views": {
        "L3": {
            "map": (function(doc) {
                if (doc._id.indexOf('R:') !== 0) return
                if (doc.L !== 3) return
                emit([doc.classifier, doc.beta], doc)
            }).toString()
        },
        "b01": {
            "map": (function(doc) {
                if (doc._id.indexOf('R:') !== 0) return
                if (doc.beta !== 0.1) return
                emit([doc.classifier, doc.L], doc)
            }).toString()
        }
    },
    "lists": {
        "csv_b": (function(head, req) {
            start({
                'headers': {
                    'Content-Type': 'text/csv'
                }
            });
            var header = false;
            while (row = getRow()) {
                if (!header) {
                      send('# classifier: ' + row.value.classifier + "\n");
                      send('# beta:       ' + row.value.beta + "\n");
                      send("#\n");
                      send("#\n");
                      header = true;
                }
                var a = []; for (var i = 0; i < row.value.results.length; ++i) a.push(row.value.results[i]);

                send(row.value.L + "," + a.join(',') + "\n");
            }
        }).toString(),
        "csv_lStat_b": (function(head, req) {
            start({
                'headers': {
                    'Content-Type': 'text/csv'
                }
            });
            var header = false;
            while (row = getRow()) {
                if (!header) {
                      send('# classifier: ' + row.value.classifier + "\n");
                      send('# beta:       ' + row.value.beta + "\n");
                      send("#\n");
                      send("#\n");
                      header = true;
                }
                var a = []; for (var i = 0; i < row.value.stat.length; ++i) a.push(row.value.stat[i]);

                send(row.value.L + "," + a.join(',') + "\n");
            }
        }).toString(),
        "csv_l": (function(head, req) {
            start({
                'headers': {
                    'Content-Type': 'text/csv'
                }
            });
            var header = false;
            while (row = getRow()) {
                if (!header) {
                      send('# classifier: ' + row.value.classifier + "\n");
                      send('# L:          ' + row.value.L + "\n");
                      send("#\n");
                      send("#\n");
                      header = true;
                }
                var a = []; for (var i = 0; i < row.value.results.length; ++i) a.push(row.value.results[i]);

                send(row.value.beta + "," + a.join(',') + "\n");
            }
        }).toString(),
        "csv_LStat_l": (function(head, req) {
            start({
                'headers': {
                    'Content-Type': 'text/csv'
                }
            });
            var header = false;
            while (row = getRow()) {
                if (!header) {
                      send('# classifier: ' + row.value.classifier + "\n");
                      send('# L:          ' + row.value.L + "\n");
                      send("#\n");
                      send("#\n");
                      header = true;
                }
                var a = []; for (var i = 0; i < row.value.stat.length; ++i) a.push(row.value.stat[i]);

                send(row.value.beta + "," + a.join(',') + "\n");
            }
        }).toString()
    }
}
