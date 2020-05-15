(function() {
  'use strict';

  angular.module('ExportScores', ['ChoirApi'])
          .controller('ExportScoresCtrl', ["$filter", 'choirApi', ExportScoresCtrl]);

  function ExportScoresCtrl($filter, choirApi) {
    var vm = this;

    // Get the current site
    var siteId = null;
    if (window.parent.getSiteId() != null) {
      siteId = window.parent.getSiteId();
    }
    vm.fromDt = new Date(window.parent.getFromDt());
    vm.toDt = new Date(window.parent.getToDt());
    // Api info
    vm.apiUrl = '';
    if (window.parent.choirApiV10 != null) {
      vm.apiUrl = window.parent.choirApiV10();
    }
    vm.siteId = siteId;

    vm.message = " ..Retrieving data, this may take a few minutes ... ";
    vm.reportDataSet = null;
    vm.downloadFile = downloadFile;
    vm.downloadBtnMsg = null;
    vm.downloadBtnClass = "btn btn-sm btn-primary";
    window.parent.showLoadingPopup();

    // Create parameter array to send to server
    var params = {
      "fromDt": vm.fromDt,
      "toDt": vm.toDt
    };
    choirApi.getReport("exportScores", params, function (result) {
      vm.reportDataSet = result.reportDataSet;
      vm.message = " ";
      if (vm.reportDataSet != null && vm.reportDataSet.length > 0) {
        vm.downloadBtnMsg = "Download File";
      }
      window.parent.hideLoadingPopup();
    });

    function downloadFile() {
      vm.downloadBtnClass = "btn btn-sm btn-default disabled";
      vm.downloadBtnMsg = "File Downloaded";
      var csvRows = [];

      if ( vm.reportDataSet !== null ) {

        for (var cell = 0; cell < vm.reportDataSet.length; ++cell) {
          csvRows.push(vm.reportDataSet[cell].join(','));
        }

        var csvString = csvRows.join("\n");
        var csvFile = new Blob([csvString], {type: "text/csv"});
        var downloadLink = document.createElement("a");
        downloadLink.download = 'ScoresExport.csv';
        downloadLink.href = window.URL.createObjectURL(csvFile);
        downloadLink.style.display = "none";
        document.body.appendChild(downloadLink);
        downloadLink.click();
      }
    }
  }
})();