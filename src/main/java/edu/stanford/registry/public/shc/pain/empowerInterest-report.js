(function() {
  'use strict';

  angular.module('EmpowerInterestReport', ['ChoirApi'])
          .controller('EmpowerInterestReportCtrl', ['$filter','choirApi', EmpowerInterestReportCtrl]);

  function EmpowerInterestReportCtrl($filter, choirApi) {
    var vm = this;
    vm.fromDt = null;
    vm.toDt = null;

    if (window.parent.getFromTm != null) {
      vm.fromDt = formatDateDisplay(new Date(Number(window.parent.getFromTm())));
    }
    if (window.parent.getToTm != null) {
      vm.toDt =  formatDateDisplay(new Date(Number(window.parent.getToTm())));
    }

    vm.form = null;
    vm.arrayLines = null;

    // methods
    vm.runReport = runReport;

    runReport();

    function runReport() {
        var jsonData = {};
        var fromParts = vm.fromDt.split("/");
        jsonData["fromDt"] = fromParts[2] + "-" + fromParts[0] + "-" + fromParts[1];
        var toParts = vm.toDt.split("/");
        jsonData["toDt"] = toParts[2] + "-" + toParts[0] + "-" + toParts[1];
        choirApi.getReport("empowerInterest", JSON.stringify(jsonData), function (result) {
        vm.arrayLines = result.reportDataSet;
        });
    }
  }
})();
