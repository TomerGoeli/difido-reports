function statusBarsController(bars){
    tests = collectAllTests();
    if (tests.length == 0) {
    	return;
    }
    var totalExecuted = tests.length;
    var success = 0;
    var failure = 0;
    var warning = 0;
    $(tests).each(function() {
        switch (this.status) {
            case "success":
                success++;
                break;
            case "error":
                failure++;
                break;
            case "failure":
                failure++;
                break;
            case "warning":
                warning++;
                break;
        }

        
    });

    function calculatePercent(part,whole) {
    	//var percent = part / tests.length * 100;
        var percent = part / whole * 100;
    	if (percent > 0 && percent <= 2) {
    		percent = 2;
    	}
    	return Math.round(percent) + "%";

    };

    function renderPercentageText(part,whole) {
    	//var percent = part / tests.length * 100;
        var percent = part / whole * 100;
    	if (percent > 0 && percent < 1) {
    		return "<1%";
    	}
   		percent = Math.round(percent);
    	if (percent <= 5){
    		return percent +"%";
    	} else {
    		return percent + "% (" + part + ")";
    	}

    }

    function getAllPlannedTests(){
        var total = 0;
        try{
            $(execution.machines).each(function() {
                total+= this.plannedTests;
            });
            
            return total;
        }
        catch(err){
            return totalExecuted;
        }
    }

    var totalPlanned = getAllPlannedTests();
    $(".totalExecuted").animate({
        width: calculatePercent(totalExecuted, totalPlanned)
    },100).text(totalExecuted + " of " + totalPlanned);
    $(".success").animate({
        width: calculatePercent(success,totalExecuted)
    },100).text(renderPercentageText(success,totalExecuted));
    $(".failure").animate({
        width: calculatePercent(failure,totalExecuted)
    }, 100).text(renderPercentageText(failure,totalExecuted));
    $(".warning").animate({
        width: calculatePercent(warning,totalExecuted)
    }, 100).text(renderPercentageText(warning,totalExecuted));
}


