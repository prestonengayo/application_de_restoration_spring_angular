import { Component, NgModule, OnInit } from '@angular/core';
import { NgApexchartsModule } from 'ng-apexcharts';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { DashboardService } from '../../core/dashboard/dashboard.service';
import { CommonModule } from '@angular/common';
import { saveAs } from 'file-saver'; // Importer file-saver

@Component({
  selector: 'app-home-dashboard',
  standalone: true,
  imports: [NgApexchartsModule, CommonModule],
  templateUrl: './home-dashboard.component.html',
  styleUrls: ['./home-dashboard.component.scss'],
})
export class HomeDashboardComponent implements OnInit {
  public chartOptions: any;
  public salesChartOptions: any;
  public categories: any[] = [];
  public orders: any[] = [];
  isDropdownVisible!: boolean;
  showAlert = false; // Pour contrôler la visibilité de l'alerte

  constructor(private dataService: DashboardService) {}

  ngOnInit(): void {
    this.loadData();
  }

  // Charger les catégories et les commandes
  loadData() {
    this.dataService.getCategories().subscribe((categories: any[]) => {
      this.categories = categories;
      this.updateChartOptions();
    });

    this.dataService.getOrders().subscribe((orders: any[]) => {
      this.orders = orders;
      this.updateSalesChartOptions();
    });
  }

  // Mettre à jour les options du graphique en fonction des catégories
  updateChartOptions() {
    const labels = this.categories.map(category => category.name);
    const series = this.categories.map(category =>
      category.menuItems.reduce((total: number, item: any) => total + item.price, 0)
    );

    this.chartOptions = {
      series: series,
      colors: ['#1C64F2', '#16BDCA', '#FDBA8C', '#E74694'],
      chart: {
        height: 320,
        width: '100%',
        type: 'donut',
      },
      stroke: { colors: ['transparent'] },
      plotOptions: {
        pie: {
          donut: {
            labels: {
              show: true,
              total: {
                show: true,
                label: 'Total',
                formatter: (w: any) =>
                  w.globals.seriesTotals.reduce((a: any, b: any) => a + b, 0),
              },
            },
          },
        },
      },
      labels: labels,
      legend: { position: 'bottom' },
    };
  }

  // Mettre à jour les options du graphique en fonction des ventes
  updateSalesChartOptions() {
    const categories = this.categories.map(category => category.name);
    const sales = categories.map(category =>
      this.orders.reduce((total: number, order: any) => {
        const itemsInCategory = order.orderItems.filter((item: any) =>
          this.isMenuItemInCategory(item.menuItem.id, category)
        );
        return total + itemsInCategory.reduce(
          (sum: number, item: any) => sum + item.totalPrice,
          0
        );
      }, 0)
    );

    this.salesChartOptions = {
      series: [
        {
          name: 'Ventes par catégorie',
          data: sales,
        },
      ],
      chart: {
        type: 'bar',
        height: 320,
      },
      xaxis: {
        categories: categories,
      },
      yaxis: {
        labels: {
          formatter: (value: number) => '$' + value,
        },
      },
      fill: {
        type: 'solid',
      },
      dataLabels: {
        enabled: false,
      },
      stroke: {
        width: 2,
      },
    };
  }

  // Méthode pour vérifier si un élément de menu appartient à une catégorie
  isMenuItemInCategory(menuItemId: number, categoryName: string): boolean {
    const category = this.categories.find(cat => cat.name === categoryName);
    return category?.menuItems.some((item: { id: number; }) => item.id === menuItemId);
  }

  // Méthode pour télécharger en PDF avec les deux graphiques et titres
// Méthode pour télécharger en PDF avec les deux graphiques et titres
downloadPDF() {
  const donutChart = document.getElementById('donut-chart')!;
  const barChart = document.getElementById('labels-chart')!;

  html2canvas(donutChart).then((canvasDonut) => {
    html2canvas(barChart).then((canvasBar) => {
      const fileWidth = 208; // Largeur du PDF en mm
      const donutHeight = (canvasDonut.height * fileWidth) / canvasDonut.width;
      const barHeight = (canvasBar.height * fileWidth) / canvasBar.width;

      // Ajustement de la hauteur totale pour inclure les deux graphiques
      const totalHeight = donutHeight + barHeight + 60; // Ajout d'un espace entre les graphiques et les titres

      let PDF = new jsPDF('p', 'mm', [fileWidth, totalHeight]);

      // Ajouter le premier graphique (Donut) avec son titre
      PDF.setFontSize(16);
      PDF.text('Répartition des ventes par catégorie (Donut)', 10, 10);
      PDF.addImage(canvasDonut.toDataURL('image/png'), 'PNG', 0, 20, fileWidth, donutHeight);

      // Ajouter le second graphique (Bar) avec son titre
      PDF.text('Ventes par catégorie (Barre)', 10, donutHeight + 30);
      PDF.addImage(canvasBar.toDataURL('image/png'), 'PNG', 0, donutHeight + 40, fileWidth, barHeight);

      PDF.save('statistiques_ventes.pdf');
      this.showSuccessAlert(); // Afficher l'alerte
    });
  });
}


  // Méthode pour télécharger en CSV
  downloadCSV() {
    let csvData = this.generateCSVData();
    let blob = new Blob([csvData], { type: 'text/csv;charset=utf-8;' });
    saveAs(blob, 'statistiques_ventes.csv'); // Utiliser saveAs de file-saver
    this.showSuccessAlert(); // Afficher l'alerte
  }

  generateCSVData(): string {
    let csv = 'Catégorie,Quantité,Ventes Totales\n';
    let salesData = this.salesChartOptions.series[0].data.map((total: number, index: number) => ({
      category: this.salesChartOptions.xaxis.categories[index],
      total,
    }));
    salesData.forEach((data: any) => {
      csv += `${data.category},${data.total}\n`;
    });
    return csv;
  }

  // Méthode pour télécharger en image avec les deux graphiques et titres
  downloadImage() {
    const donutChart = document.getElementById('donut-chart')!;
    const barChart = document.getElementById('labels-chart')!;

    html2canvas(donutChart).then((canvasDonut) => {
      html2canvas(barChart).then((canvasBar) => {
        const link = document.createElement('a');
        const combinedCanvas = document.createElement('canvas');
        combinedCanvas.width = Math.max(canvasDonut.width, canvasBar.width);
        combinedCanvas.height = canvasDonut.height + canvasBar.height;

        const ctx = combinedCanvas.getContext('2d');
        if (ctx) {
          ctx.font = '16px Arial';
          ctx.fillText('Répartition des ventes par catégorie (Donut)', 10, 20);
          ctx.drawImage(canvasDonut, 0, 40);
          ctx.fillText('Ventes par catégorie (Barre)', 10, canvasDonut.height + 60);
          ctx.drawImage(canvasBar, 0, canvasDonut.height + 80);
        }

        link.href = combinedCanvas.toDataURL('image/png');
        link.download = 'statistiques_ventes.png';
        link.click();
        this.showSuccessAlert(); // Afficher l'alerte
      });
    });
  }

  // Méthode pour basculer la visibilité du menu déroulant
  toggleDropdown() {
    this.isDropdownVisible = !this.isDropdownVisible;
  }

  // Méthode pour afficher l'alerte de succès
  showSuccessAlert() {
    this.showAlert = true;
    setTimeout(() => {
      this.showAlert = false;
    }, 3000); // L'alerte disparaît après 3 secondes
  }
}
