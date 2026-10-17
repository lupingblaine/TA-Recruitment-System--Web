<%@ page pageEncoding="UTF-8" %>
<div id="applicationPeriodBanner" role="status" aria-live="polite" class="rounded-xl border border-zinc-200 bg-zinc-50 p-4 mb-6 text-sm text-zinc-700">
  Checking application period…
</div>
<script>
document.addEventListener('DOMContentLoaded', async function() {
  const banner = document.getElementById('applicationPeriodBanner');
  try {
    const state = await applicantApi.getApplicationPeriod();
    const period = state.period;
    const labels = {open: 'Applications open', scheduled: 'Applications not yet open', expired: 'Application deadline passed', closed: 'Applications closed', none: 'No application period is open'};
    let text = labels[state.effectiveStatus] || 'Applications closed';
    if (period) {
      text += ' · ' + period.name;
      if (period.termName) text += ' · ' + period.termName;
      const options = { dateStyle: 'medium', timeStyle: 'short' };
      if (state.effectiveStatus === 'scheduled') text += ' · Opens ' + new Date(period.opensAt).toLocaleString(undefined, options);
      text += ' · Deadline ' + new Date(period.closesAt).toLocaleString(undefined, options);
      text += ' (' + Intl.DateTimeFormat().resolvedOptions().timeZone + ')';
    }
    if (!state.accepting) text += '. You can still view your applications.';
    banner.textContent = text;
    banner.classList.toggle('bg-emerald-50', state.accepting);
  } catch (error) {
    banner.textContent = 'Could not check the application period. Refresh before submitting.';
  }
});
</script>
