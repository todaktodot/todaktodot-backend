(function () {
    if (typeof echarts === 'undefined' || !window.statisticsChartData) {
        return;
    }

    const data = window.statisticsChartData;
    const chartInstances = [];
    const palette = {
        blue: '#3498db',
        purple: '#7740AE',
        green: '#27ae60',
        orange: '#f39c12',
        red: '#e74c3c',
        slate: '#5a6c7d'
    };

    function number(value) {
        return Number(value || 0).toLocaleString();
    }

    function percent(value) {
        return `${Number(value || 0).toLocaleString(undefined, { maximumFractionDigits: 2 })}%`;
    }

    function tooltipBox(title, rows) {
        return `
            <div class="statistics-chart-tooltip">
                <div class="tooltip-title">${title}</div>
                ${rows.map(function (row) {
                    return `<div class="tooltip-row"><span>${row.marker}${row.name}</span><strong>${row.value}</strong></div>`;
                }).join('')}
            </div>
        `;
    }

    function initChart(elementId, option) {
        const element = document.getElementById(elementId);
        if (!element) {
            return;
        }
        const chart = echarts.init(element, null, { renderer: 'canvas' });
        chart.setOption(option);
        chartInstances.push(chart);
    }

    const commonGrid = { top: 54, right: 24, bottom: 34, left: 58 };
    const commonTooltip = {
        trigger: 'axis',
        axisPointer: { type: 'line', lineStyle: { color: '#dce4ec' } },
        backgroundColor: 'rgba(255, 255, 255, 0.98)',
        borderColor: '#e1e8ed',
        borderWidth: 1,
        padding: 0,
        extraCssText: 'box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12); border-radius: 12px; overflow: hidden;'
    };

    initChart('userCoupleChart', {
        color: [palette.blue, palette.purple],
        tooltip: {
            ...commonTooltip,
            formatter: function (params) {
                return tooltipBox(params[0].axisValue, params.map(function (item) {
                    return { marker: item.marker, name: item.seriesName, value: number(item.value) };
                }));
            }
        },
        legend: { top: 0, icon: 'roundRect' },
        grid: commonGrid,
        xAxis: { type: 'category', data: data.labels || [], boundaryGap: false },
        yAxis: { type: 'value', axisLabel: { formatter: function (value) { return number(value); } } },
        series: [
            {
                name: '전체 유저 수',
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                areaStyle: { opacity: 0.08 },
                emphasis: { focus: 'series' },
                data: data.totalUsers || []
            },
            {
                name: '전체 커플 수',
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                areaStyle: { opacity: 0.08 },
                emphasis: { focus: 'series' },
                data: data.totalCouples || []
            }
        ],
        animationDuration: 700,
        animationEasing: 'cubicOut'
    });

    initChart('answerVolumeChart', {
        color: [palette.green, palette.orange],
        tooltip: {
            ...commonTooltip,
            axisPointer: { type: 'shadow' },
            formatter: function (params) {
                return tooltipBox(params[0].axisValue, params.map(function (item) {
                    return { marker: item.marker, name: item.seriesName, value: number(item.value) };
                }));
            }
        },
        legend: { top: 0, icon: 'roundRect' },
        grid: commonGrid,
        xAxis: { type: 'category', data: data.labels || [] },
        yAxis: { type: 'value', axisLabel: { formatter: function (value) { return number(value); } } },
        series: [
            { name: '선택 완료 데일리카드 수', type: 'bar', barMaxWidth: 28, data: data.dailyCards || [] },
            { name: '답변 완료 유저 수', type: 'bar', barMaxWidth: 28, data: data.answeredUsers || [] }
        ],
        animationDuration: 700,
        animationEasing: 'cubicOut'
    });

    initChart('answerRateChart', {
        color: [palette.blue, palette.purple],
        tooltip: {
            ...commonTooltip,
            formatter: function (params) {
                return tooltipBox(params[0].axisValue, params.map(function (item) {
                    return { marker: item.marker, name: item.seriesName, value: percent(item.value) };
                }));
            }
        },
        legend: { top: 0, icon: 'roundRect' },
        grid: commonGrid,
        xAxis: { type: 'category', data: data.labels || [], boundaryGap: false },
        yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
        series: [
            {
                name: '개인 답변 완료율',
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                emphasis: { focus: 'series' },
                data: data.personalAnswerRates || []
            },
            {
                name: '커플 둘 다 완료율',
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                emphasis: { focus: 'series' },
                data: data.coupleBothAnswerRates || []
            }
        ],
        animationDuration: 700,
        animationEasing: 'cubicOut'
    });

    initChart('feedbackChart', {
        color: [palette.purple],
        tooltip: {
            ...commonTooltip,
            axisPointer: { type: 'shadow' },
            formatter: function (params) {
                return tooltipBox(params[0].axisValue, params.map(function (item) {
                    return { marker: item.marker, name: item.seriesName, value: number(item.value) };
                }));
            }
        },
        grid: commonGrid,
        xAxis: { type: 'category', data: data.labels || [] },
        yAxis: { type: 'value', axisLabel: { formatter: function (value) { return number(value); } } },
        series: [
            {
                name: 'AI 피드백 생성 수',
                type: 'bar',
                barMaxWidth: 34,
                data: data.aiFeedbacks || [],
                itemStyle: { borderRadius: [8, 8, 0, 0] }
            }
        ],
        animationDuration: 700,
        animationEasing: 'cubicOut'
    });

    window.addEventListener('resize', function () {
        chartInstances.forEach(function (chart) {
            chart.resize();
        });
    });
})();
