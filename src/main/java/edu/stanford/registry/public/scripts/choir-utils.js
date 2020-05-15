// Common utility functions

  /*
   Function returns a 'MM/dd/yyyy' string for the
    epochtime: (long) time
   */
  function formatDateDisplay(epochtime) {
    var date = new Date(epochtime);
    var yr = date.getFullYear();
    var mth = padZero(date.getMonth() + 1);
    var day = padZero(date.getDate());
    return mth + "/" + day + "/" + yr;
  }

  /*
    Function returns a date object from the parameters
     stringdate:  'MM/dd/yyyy' (string)
     hour: hour of the day
     min: minutes of the hour
    For example to get the start of a date use hour=0, min=0
    And for the end of a day use hour=23, min=59
   */
  function formatDateParam(stringdate, hour, min) {
    var dateParts = stringdate.split("/");
    // return a new Date(year, month index, day, hour, min)
    return new Date(dateParts[2], dateParts[0] - 1, dateParts[1], hour, min);
  }

  function padZero(val) {
    if (val < 10) {
      val = "0" + val;
    }
    return val;
  }