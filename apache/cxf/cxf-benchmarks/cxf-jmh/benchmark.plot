# Labels
set title 'cxf-jmh'
set ylabel 'ms/op'
set xlabel 'Tested scenario'
set xtics nomirror rotate by -90

# Ranges
set autoscale

# Input
set datafile separator ','

# Output
set terminal pngcairo enhanced font "Verdana,9"
set output 'results.png'
set grid
set key off
set boxwidth 0.8 relative

# box style
set style line 1 lc rgb '#5C91CD' lt 1
set style fill solid

# remove top and right borders
set style line 2 lc rgb '#808080' lt 1
set border 3 back ls 2
set tics nomirror

plot 'results.csv' every ::1 using 0:5:xticlabels(stringcolumn(1)[74:]) with boxes ls 1,\
    'results.csv' every ::1 using 0:($5 + 1):(sprintf("%d",$5)) with labels
