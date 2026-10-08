import { defineConfig } from '@playwright/test';
export default defineConfig({
  testDir:'./tests',timeout:30000,workers:1,
  use:{baseURL:'http://127.0.0.1:18889',browserName:'chromium',trace:'retain-on-failure'},
  webServer:{command:'node ../../scripts/enigma-demo.mjs 18889',url:'http://127.0.0.1:18889',reuseExistingServer:false,timeout:30000},
});
