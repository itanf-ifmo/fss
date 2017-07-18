var https = require('https');
var DB = 'fssas-distances'

var views = {
  'values': {
    map: function (doc) {
      var ids = doc._id.split(":");
      if (ids[0] !== 'orig' && ids[0] !== 'cm') {
        return;
      }
      ids[1] = '';
      emit(ids, [doc.dataset, doc.value]);
    },
    reduce: function (keys, values, rereduce) {
      if (rereduce) {
        return [].concat.apply([], values);
      } else {
        return values;
      }
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
  https.request({
    host: 'couchdb.home.tanfilyev.ru',
    path: '/' + DB + '/' + doc._id,
    headers: {
      'Authorization': 'Basic ' + new Buffer('admin:changeme42').toString('base64')
    }
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
  var req = https.request({
    host: 'couchdb.home.tanfilyev.ru',
    path: '/' + DB + '/' + doc._id,
    method: 'PUT',
    headers: {
      'Authorization': 'Basic ' + new Buffer('admin:changeme42').toString('base64')
    }
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
