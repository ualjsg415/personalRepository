import {Routes} from '@angular/router';
import {Home} from './home/home';
import {Details} from './details/details';
const routeConfig: Routes = [
  {
    path: '',
    component: Home,
    title: 'LCK Match Cup 2026',
  },
  {
    path: 'details/:id',
    component: Details,
    title: 'Detalle del enfrentamiento',
  },
];
export default routeConfig;