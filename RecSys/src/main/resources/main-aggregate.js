var http = require('http');

var views = {
  aggregate: {
    map: function (doc) {
      emit(doc.dataset, doc);
    },
    reduce: function (keys, values, rereduce) {
      var v = {};
      values.forEach(function (i) {
        if (rereduce) {
          Object.keys(i).forEach(function (j) {
            v[j] = i[j];
          });
        } else {
          v.dataset = i.dataset;
          v[i.method] = i.data;
        }
      });
      return v;
    }
  }
}

var doc = {
  _id: '_design/main',
  views: views,
  language: "javascript"
}

Object.keys(doc.views).forEach(function(viewName) {
  doc.views[viewName].map = doc.views[viewName].map && doc.views[viewName].map.toString();
  doc.views[viewName].reduce = doc.views[viewName].reduce && doc.views[viewName].reduce.toString();
});

function getRev(doc, callback) {
  http.request({
    host: 'localhost',
    port: 5984,
    path: '/classifiers-comparison/' + doc._id
  }, function (response) {
    var str = '';
    response.on('data', function (chunk) {
      str += chunk;
    });
    response.on('end', function () {
      doc._rev = JSON.parse(str)._rev
      callback();
    });
  }).end();
}

getRev(doc, function () {
  var req = http.request({
    host: 'localhost',
    port: 5984,
    path: '/classifiers-comparison/' + doc._id,
    method: 'PUT'
  }, function (response) {
    var str = '';
    response.on('data', function (chunk) {
      str += chunk;
    });
    response.on('end', function () {
      console.log(JSON.stringify(JSON.parse(str), null, 2));
    });
  });
  req.write(JSON.stringify(doc));
  req.end();
});
