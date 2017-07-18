var https = require('https');
var DB = 'classifiers-v3'

var views = {
  'mean-AEARR-1': {
    map: function (doc) {
      var s = 0;
      for (var i = 0; i < 1; i++) {
        s += doc.data[i]
      }
      emit(doc._id.split(':'), s);
    },
    reduce: function (keys, values) {
      var s = 0;
      values.forEach(function (i) {
        s += i
      });
      return s;
    }
  },
  'mean-AEARR-3': {
    map: function (doc) {
      var s = 0;
      for (var i = 0; i < 3; i++) {
        s += doc.data[i]
      }
      emit(doc._id.split(':'), s / 3);
    },
    reduce: function (keys, values) {
      var s = 0;
      values.forEach(function (i) {
        s += i
      });
      return s;
    }
  },
  'mean-AEARR-5': {
    map: function (doc) {
      var s = 0;
      for (var i = 0; i < 5; i++) {
        s += doc.data[i]
      }
      emit(doc._id.split(':'), s / 5);
    },
    reduce: function (keys, values) {
      var s = 0;
      values.forEach(function (i) {
        s += i
      });
      return s;
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
