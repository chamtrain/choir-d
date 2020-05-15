(function () {
  'use strict';

  angular
    .module('PatientLatestSurveyReport', ['ChoirApi'])
    .controller('PatientLatestSurveyReportCtrl', ['choirApi', PatientLatestSurveyReportCtrl]);

  var patientId = window.parent.getPatientId();

  function PatientLatestSurveyReportCtrl(choirApi) {
    var patientReport = this;
    patientReport.latestSurvey = null;
    patientReport.refresh = refresh;
    patientReport.instrumentChartHandler = instrumentChartHandler;

    refresh();

    function refresh() {
      patientReport.latestSurvey = null;
      patientReport.instrumentTotalScore = null;

      try {
        choirApi.getPatientReport(patientId, function (result) {
          if (result != null) {
            if(!result.hasOwnProperty("error")){
              patientReport.latestSurvey = result;
              patientReport.instrumentTotalScore = getInstrumentsTotalScores(result);
              //display main chart
              instrumentChartHandler("all");
            }else{
              patientReport.error = result.error;
            }
          }
        });
      } catch (e) {
        console.error(e)
      }
    }

    // display chart for the respective tab
    function instrumentChartHandler(e) {
      if (e === 'all') {
        // all instrument tab
        if ($("#all .all-instrument-chart").children().length === 0) {
          var allChartSource = getAllInstrumentSource(patientReport.instrumentTotalScore);
          chartScore(allChartSource, 'All');
        }
      } else
        if (e.hasOwnProperty('instrument') && e.instrument.hasOwnProperty('name') && e.instrument.name !== '') {
          if ($("#" + e.instrument.name + " .instrument-chart").children().length === 0) {
            //chart not loaded
            var currStudy;
            patientReport.instrumentTotalScore.forEach(function (v) {
              if (v.name === e.instrument.name) {
                currStudy = v;
              }
            });

            if (typeof currStudy !== 'undefined') {
              if (currStudy.name === e.instrument.name && currStudy.hasOwnProperty("hasSubScale")) {
                chartScore(getSubScalesSource(patientReport.instrumentTotalScore, e.instrument.name), 'Sub-scale');
              } else {
                var chartSource = getAllInstrumentSource([currStudy]);
                chartSource.name = e.instrument.name;
                chartScore(chartSource, 'Instrument');
              }
            }
          }
        }
    }
  }

  // chart data source for all instruments
  function getAllInstrumentSource(instrumentTotalScore) {
    var allAssessmentDataSource = [];

    instrumentTotalScore.forEach(function (instrument) {
      var instEntry = new InstrumentScale(instrument.name);
      instrument.assessments.forEach(function (assessment) {
        instEntry.scores.push({
          sDate: assessment.sDate,
          score: assessment.totalScore
        });
      });
      allAssessmentDataSource.push(instEntry);
    });

    return {
      // Minimum and Maximum scores/dates used to draw graph axis
      edgeValues: getEdgeValues(allAssessmentDataSource),
      dataSource: allAssessmentDataSource
    };

    // Sub-scale prototype
    function InstrumentScale(name) {
      this.name = name;
      this.scores = [];
    }
  }

  // checks if the current score has sub-scale scores
  function hasSubScale(curScore, studyDescription) {
    try {
      if (curScore.hasOwnProperty('SCORES') && curScore.SCORES.length > 0) {
        if (curScore.SCORES.length === 1) {
          if (curScore.SCORES[0].ScoreTitle === studyDescription || curScore.SCORES[0].ScoreTitle === "Total") {
            return false;
          }
        }
        return true;
      }
    } catch (e) {
      console.error(e)
    }
    return false;
  }

  function getInstrumentsTotalScores(result) {
    // list: is a json result from the API call
    var wrapper = [];

    if (result.hasOwnProperty('Studies')) {
      for (var i = 0; i < result.Studies.length; i++) {
        if (result.Studies[i].hasOwnProperty('AllScores') && result.Studies[i].hasOwnProperty('StudyDescription')) {
          var currentScores = result.Studies[i].AllScores;

          // Check to see if the current study has score
          if (currentScores != null && currentScores.length > 0) {
            // Iterate through the scores in the ScoreTable
            var studyDescription = result.Studies[i].StudyDescription;
            var entry = new Score(studyDescription);

            // Compose the new score object
            currentScores.forEach(function (item) {
              if (item.hasOwnProperty('SCORES')) {
                // If the assessment has sub scales, start building the object together with sub scale name and score
                var ss = [];
                if (hasSubScale(item, studyDescription)) {
                  entry.hasSubScale = true;
                  var subscaleTitles = getUniqueSubScaleNames(result, studyDescription);
                  subscaleTitles.forEach(function (title) {
                    ss.push({
                      name: title,
                      score: item.SCORES.find(function (v) {
                        return v.ScoreTitle === title;
                      }).ScoreValue
                    });
                  });
                }

                var totalScore;
                for (var i = 0; i < item.SCORES.length; i++) {
                  if (item.SCORES[i].ScoreTitle === "Total" || item.SCORES[i].ScoreTitle === studyDescription) {
                    totalScore = item.SCORES[i].ScoreValue;
                  }
                }

                // Join study and the sub scales, if it has any
                if (item.hasOwnProperty('ScoreDate')) {
                  var ssEntry = {
                    sDate: item.ScoreDate,
                    totalScore: totalScore,
                    subScales: ss
                  };
                }
                entry.assessments.push(ssEntry);
              }
            });
            // Add survey score object: this object is the complete score(includes sub scales) for a study in a survey
            wrapper.push(entry);
          }
        }

      }
    }

    return wrapper;

    // Score object
    function Score(name) {
      this.name = name;
      this.assessments = [];
    }
  }

  // Sub-scale chart datasource driver
  function getSubScalesSource(instrumentTotalScore, instrument) {

    var subScaleNames = [];
    var subScaleDataSource = [];

    var curStudy = instrumentTotalScore.find(function (l) {
      return l.name === instrument;
    });

    curStudy.assessments.forEach(function (a) {
      a.subScales.forEach(function (s) {
        if (subScaleNames.indexOf(s.name) === -1 && s.name !== "Total") {
          subScaleNames.push(s.name);
        }
      });
    });

    // check if the current instrument does not have sub-scales
    if (subScaleNames.length === 0) {
      return null;
    }

    subScaleNames.forEach(function (ssName) {
      var ssEntry = new SubScale(ssName);
      curStudy.assessments.forEach(function (assessment) {
        ssEntry.scores.push({
          sDate: assessment.sDate,
          score: assessment.subScales.find(function (v) {
            return v.name === ssName
          }).score
        });
      });
      subScaleDataSource.push(ssEntry)
    });

    return {
      // Minimum and Maximum scores/dates used to draw sub scales graph
      name: instrument,
      edgeValues: getEdgeValues(subScaleDataSource),
      dataSource: subScaleDataSource
    };

    // Sub-scale prototype
    function SubScale(name) {
      this.name = name;
      this.scores = [];
    }
  }

  // Returns min and max date and score values
  function getEdgeValues(subScaleDataSource) {
    var edgeValues = {
      sDate: { min: 9999999999999999, max: -9999999999999999 },
      score: { min: 9999999999999999, max: -9999999999999999 }
    };

    subScaleDataSource.forEach(function (entry) {
      entry.scores.forEach(function (v) {
        var d = new Date(v.sDate + " ");
        var s = v.score;
        var tol = (s < 10) ? 1 : Math.round(s / (10));

        if (edgeValues.sDate.min > d) {
          edgeValues.sDate.min = d;
        }
        if (edgeValues.sDate.max < d) {
          edgeValues.sDate.max = d;
        }

        if (edgeValues.score.min > s) {
          edgeValues.score.min = 0;
        }
        if (edgeValues.score.max < s) {
          edgeValues.score.max = s + tol;
        }
      });
    });
    return edgeValues;
  }

  function getUniqueSubScaleNames(result, instrument) {
    var subScaleNames = [];
    for (var i = 0; i < result.Studies.length; i++) {
      if (result.Studies[i].StudyDescription === instrument) {
        result.Studies[i].AllScores.forEach(function (scoreHistory) {
          scoreHistory.SCORES.forEach(function (score) {
            if (subScaleNames.indexOf(score.ScoreTitle) === -1 && score.ScoreTitle !== "Total") {
              subScaleNames.push(score.ScoreTitle);
            }
          });
        });
        // found; no need to iterate further
        return subScaleNames;
      }
    }
    return subScaleNames;
  }

  // Draw sub scales chart from datasource
  function chartScore(list, sourceType) {
    var margin = { top: 50, right: 50, bottom: 50, left: 50 };
    var width = 800;
    var height = 300;

    var dateFormatter = d3.timeParse("%m/%d/%Y");
    var tooltipDateFormatter = d3.timeFormat("%B %d, %Y");

    var unGroupedList = [];
    list.dataSource.forEach(function (entries) {
      entries.scores.forEach(function (value) {
        unGroupedList.push({
          name: entries.name,
          sDate: dateFormatter(value.sDate),
          score: value.score
        });
      })
    });

    var x = d3.scaleTime().range([0, 600]);
    var y = d3.scaleLinear().range([height, 0]);

    // Scale the range of the data
    x.domain([list.edgeValues.sDate.min, list.edgeValues.sDate.max]);

    y.domain([list.edgeValues.score.min, list.edgeValues.score.max]);

    var dataLine = d3.line()
      .x(function (d) {
        return x(d.sDate)
      })
      .y(function (d) {
        return y(d.score)
      })
      .curve(d3.curveMonotoneX);

    var groupedList = d3.nest()
      .key(function (d) {
        return d.name;
      })
      .entries(unGroupedList);

    var type = null;
    if (sourceType === 'All') {
      type = "#all .all-instrument-chart";
    } else {
      type = "#" + list.name + " .instrument-chart";
    }

    var svg = d3.select(type).append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var color = d3.scaleOrdinal(d3.schemeCategory10);

    // Tooltip for scores points on hover
    var tooltip = d3.select("body")
      .append("div")
      .attr("class", "tooltip")
      .style("opacity", 0);

    // Creating value lines
    groupedList.forEach(function (d) {

      svg.append("path")
        .attr("class", "line")
        .style("stroke", function () {
          return d.color = color(d.key);
        })
        .attr("d", dataLine(d.values));

      var lineLegend = svg.selectAll(".line-Legend")
        .data(groupedList.map(function (d) {
          return d.key;
        }))
        .enter().append("g")
        .attr("class", "line-Legend")
        .style("text-anchor", "start")
        .attr("transform", function (d, i) {
          return "translate(" + (width - 150) + " ," + (i * 20) + ")";
        });

      lineLegend.append("text")
        .text(function (d) {
          return d;
        })
        .attr("transform", "translate(15,9)");

      lineLegend.append("rect")
        .attr("fill", function (d, i) {
          return color(d);
        })
        .attr("width", 10).attr("height", 10);
    });

    svg.append("g")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(x)
        .tickValues(unGroupedList.map(function (f) {
          return new Date(f.sDate);
        }))
        .tickFormat(d3.timeFormat("%m/%d/%Y")));

    svg.append("g")
      .call(d3.axisLeft(y));

    svg.selectAll(".dot")
      .data(unGroupedList)
      .enter().append("circle")
      .attr("class", "dot")
      .attr("cx", function (d) {
        return x(d.sDate)
      })
      .attr("cy", function (d) {
        return y(d.score)
      })
      .attr("r", 4)
      .attr("fill", function (d) {
        return d.color = color(d.name);
      })
      .on("mouseover", function (d) {
        $(".tooltip").empty()
          .append($('<div></div>').text(sourceType + ": " + d.name))
          .append($('<div></div>').text("Survey Date: " + tooltipDateFormatter(new Date(d.sDate))))
          .append($('<div></div>').text("Score: " + d.score));
        tooltip.transition()
          .duration(200)
          .style("opacity", .9)
          .style("left", (d3.event.pageX) + "px")
          .style("top", (d3.event.pageY - 58) + "px");
      })
      .on("mouseout", function () {
        tooltip.transition()
          .duration(500)
          .style("opacity", 0);
      });
  }

})();

