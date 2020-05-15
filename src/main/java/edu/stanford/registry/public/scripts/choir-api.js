/*
 * Methods for calling the CHOIR API.
 */

(function() {
  'use strict';

  angular.module('ChoirApi', [])
          .factory('choirApi', [ '$http', createService ]);

  function apiInfo($http) {

  }

  function createService($http) {
    var apiUrl = null;
    var siteId = null;

    // Get the apiUrl and siteId from the window parent
    if (window.parent.choirApiV10 != null) {
      apiUrl = window.parent.choirApiV10();
    }
    if (window.parent.getSiteId != null) {
      siteId = window.parent.getSiteId();
    }

    // Defaults for testing in a stand alone window
    if (apiUrl == null) {
      apiUrl = 'http://localhost:8080/registry/registry/svc/apiV10/json/';
          }
    if (siteId == null) {
      siteId = 'tst';
    }

    // Return an Object containing the methods
    return {
      updatePatientAttribute: function(patId, pattr, callback) {
        var req = {
          method: 'POST',
          url: apiUrl + 'patattribute/mod/' + encodeURIComponent(patId),
          data: pattr
        };
        apiRequest(req, callback);
      },

      getPatient: function(patId, callback) {
        var req = {
          method: 'GET',
          url: apiUrl + 'patient/' + encodeURIComponent(patId)
        };
        apiRequest(req, callback);
      },

      updatePatient: function(patient, callback) {
        var req = {
          method: 'POST',
          url: apiUrl + 'patient/mod',
          data: patient
        };
        apiRequest(req, callback);
      },

      registerPatient: function(patient, callback) {
        var req = {
          method: 'POST',
          url: apiUrl + 'empower/patient/register',
          data: patient
        };
        apiRequest(req, callback);
      },

      getReport: function(report, params, callback) {
        var req = {
          method: 'POST',
          url: apiUrl + 'report/' + encodeURIComponent(report),
          data: params
        };
        apiRequest(req, callback);
      },

      storeData: function(patientId, type, value, callback) {
        var req = {
          method: 'POST',
          url: apiUrl + "pluginData/patient/post/" + encodeURIComponent(type),
          data: {patientId: patientId, dataValue: value}
        };
        apiRequest(req, callback);
      },

      getData: function(patientId, type, callback) {
        var req = {
          method: 'POST',
          url: apiUrl + "pluginData/patient/getLast/" + encodeURIComponent(type),
          data: {patientId: patientId}
        };
        apiRequest(req, callback);
      },

      updateSurveyAttribute: function(attr, callback) {
        console.log("calling API with data:" + JSON.stringify(attr));
        var req = {
          method: 'POST',
          url: apiUrl + 'surveyattribute/mod',
          data: attr
        };
        apiRequest(req, callback);
      },

      getPatientReport: function (patId, callback) {
        var req = {
          method: 'GET',
          url: apiUrl + 'patreport/patient/' + encodeURIComponent(patId)
        };
        apiRequest(req, callback);
      },

      getExtract: function(tablename, parameters, callback) {
        let urlString = apiUrl + "extract/" + encodeURIComponent(tablename) + "?siteId=" + siteId;
        var req = {
          method: 'POST',
          url: urlString,
          data: parameters
        };
        apiRequest(req, callback);
      },

      downloadReport: function( jsonObj, filename ){
        // convert json to csv
        var downloadString = '';
        if(typeof jsonObj === "object"){
          for (var i = 0; i < jsonObj.reportDataSet.length; i++) {
            var arr = jsonObj.reportDataSet[i];
            var line = '';
            for (a in arr) {
              if (line !== '') line +=',';
              line +="\"" + arr[a] +"\"";
            }
            downloadString += line + '\r\n';
          }
        }
        var blob = new Blob([downloadString], {type: 'text/csv'}),
                e    = document.createEvent('MouseEvents'),
                a    = document.createElement('a');
        a.download = filename + ".csv";
        a.href = window.URL.createObjectURL(blob);
        a.dataset.downloadurl =  ['text/csv', a.download, a.href].join(':');
        e.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
        a.dispatchEvent(e);
      }
    };

    /*
     * Make the API request
     */
    function apiRequest(req, callback) {
      // Add the Accept header
      if (req.headers == null) {
        req.headers = {};
      }
      req.headers.Accept = 'application/json';

      // Add the siteId parameter
      if (req.params == null) {
        req.params = {};
      }
      req.params.siteId = siteId;

      // Make the request
      $http(req).then(
              function(success_response) {
                callback(success_response.data);
              },
              function(error_response) {
                console.log('API Request', req);
                console.log('API FAILED', error_response);
                var data = error_response.data;
                if (typeof(data) == 'object') {
                  data = JSON.stringify(data);
                }
                window.alert('ERROR\n' + data);
              }
      );
    }
  }

})();
